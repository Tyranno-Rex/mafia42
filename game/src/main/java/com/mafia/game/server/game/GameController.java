package com.mafia.game.server.game;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.model.gamer.GamerService;
import com.mafia.game.server.game.gameDto.GameDeleteDTO;
import com.mafia.game.server.game.gameDto.GameJoinDTO;
import com.mafia.game.server.game.gameDto.GameReadyDTO;
import com.mafia.game.server.game.gameStatus.GamePlayer;
import com.mafia.game.server.game.gameStatus.GameState;
import com.mafia.game.server.socket.SocketController;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import com.mafia.game.server.game.gameDto.GameStatusDTO;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;
    private final SocketController socketController;
    private final GamerService gamerService;
    Map<Long, GameState> gameMap = new HashMap<>(); // 게임 전체의 정보를 기록
    Map<Long, GamePlayer> gamerMap = new HashMap<>(); // 게이머의 접속 시간을 기록

    @EventListener
    public void handleGamerActivityEvent(GamerActivityEvent event) {
        updateGamerActivity(event.getUsername());
    }

    @GetMapping("/all")
    public Iterable<Game> getAllGames() {
        return gameService.getActiveGames();
    }

    @PostMapping("/create")
    @Transactional
    public Map<String, String> createGame(@RequestBody GameStatusDTO gameStatusDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            // 실제 DB에 게임 정보를 저장
            Game game = gameService.createGame(gameStatusDTO);


            // gameMap에 게임 정보를 저장
            Long gameId = game.getId();
            gameMap.put(gameId, new GameState(gameId,
                    game.getRoomName(), game.getRoomPassword(),
                    game.getRoomStatus(), game.getRoomOwner(),
                    game.getRoomPlayerCount(), game.getRoomMaxPlayerCount()));

            response.put("status", "success");
            return response;
        } catch (Exception e) {
            response.put("status", "fail");
            return response;
        }
    }

    @DeleteMapping("/delete")
    @Transactional
    public Map<String, String> deleteGame(@RequestBody GameDeleteDTO gameDeleteDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            gameService.deleteGame(gameDeleteDTO);
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            response.put("status", "fail");
            return response;
        }
    }

    @PostMapping("/join")
    @Transactional
    public Map<String, String> joinGame(@RequestBody GameJoinDTO gameJoinDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            // 실제 DB에 게임 정보를 업데이트
            gameService.joinGame(gameJoinDTO);

            // gameMap에 게임 정보를 업데이트
            GameState game = new GameState();
            if (gameMap.containsKey(gameJoinDTO.getGameId())) {
                System.out.println("Game Join: " + gameJoinDTO.getUserName());
                game = gameMap.get(gameJoinDTO.getGameId());
            }
            Gamer newPlayer = gamerService.findByUserName(gameJoinDTO.getUserName());

            // 게임 안에 플레이어가 있는 지 확인
            if (game.getPlayers().stream().anyMatch(p -> p.getId().equals(newPlayer.getId()))) {
                response.put("status", "fail");
                response.put("reason", "User already joined the game");
                return response;
            } else {
                System.out.println("Game Join: " + gameJoinDTO.getUserName());
                List<Gamer> players = new ArrayList<>(game.getPlayers());
                players.add(newPlayer);
                game.setId(gameJoinDTO.getGameId());
                game.setPlayers(players);
                game.setGameStatus("WAITING");
                gameMap.put(game.getId(), game);
            }

            GamePlayer newGamePlayer = new GamePlayer(newPlayer.getUserName(), "CITIZEN", true, false);

            newGamePlayer.setDateTime(LocalDateTime.now().toString());
            // gamerMap에 게이머의 접속 시간을 기록
            gamerMap.put(newPlayer.getId(), newGamePlayer);
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            response.put("status", "fail");
            return response;
        }
    }

    @PostMapping("/ready")
    @Transactional
    public Map<String, String> readyGame(@RequestBody GameReadyDTO gameReadyDTO){
        Map<String, String> response = new HashMap<>();
        try {
            System.out.println("Game Ready      : " + gameReadyDTO.getUsername());
            GameState game = gameMap.get(gameReadyDTO.getGameId());
            List<Gamer> players = game.getPlayers();
            for (Gamer player : players) {
                if (player.getUserName().equals(gameReadyDTO.getUsername())) {
                    player.setIsReady(gameReadyDTO.getReadyStatus());
                }
            }
            game.setPlayers(players);
            gameMap.put(game.getId(), game);
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            response.put("status", "fail");
            return response;
        }
    }




    @Scheduled(fixedRate = 1000)
    @Transactional
    public void MainGameLoop() throws Exception {

        System.out.println("\nGameMap Size: " + gameMap.size());
        System.out.println("GamerMap Size: " + gamerMap.size());
        System.out.println("==================================\n");

        for (Game game : gameService.getAllGames()){
            if (!gameMap.containsKey(game.getId())) {
                game.setGameStatus("SHUTDOWN");
                gameService.saveGame(game);
            }
        }

        Iterator<Map.Entry<Long, GamePlayer>> gamerMapIterator = gamerMap.entrySet().iterator();
        while (gamerMapIterator.hasNext()) {
            Map.Entry<Long, GamePlayer> entry = gamerMapIterator.next();
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(entry.getValue().getDateTime()).plusSeconds(10))) {
                gamerMapIterator.remove();
                // GameMap의 player에서도 해당 플레이어를 제거
                for (GameState game : gameMap.values()) {
                    List<Gamer> updatedPlayers = new ArrayList<>(game.getPlayers());
                    updatedPlayers.removeIf(gamer -> gamer.getId().equals(entry.getKey()));
                    game.setPlayers(updatedPlayers);
                }
            }
        }

        for (GameState game : gameMap.values()) {
            System.out.println("Game ID: " + game.getId() + " Game Status: " + game.getGameStatus() + " Player Count: " + game.getPlayers().size());
            if (game.getGameStatus().equals("WAITING")) {
                List<Gamer> players = game.getPlayers();
                if (players.isEmpty())
                    gameMap.remove(game.getId());
                else
                    socketController.GameSocket(game.getId(), game);
            }
            else if (game.getGameStatus().equals("STARTING")) {
                game.setGameStatus("PLAYING");
                game = gameService.SetGameState(game);

                List<GamePlayer> gamePlayers = game.getGamePlayers();
                for (int i = 0; i < 8; i++){
                    System.out.println("Role: " + gamePlayers.get(i).getRole());
                }
                gameMap.put(game.getId(), game);
            }
            else if (game.getGameStatus().equals("PLAYING")) {
                game = gameService.updateGameState(game);
                gameMap.put(game.getId(), game);
                socketController.GameSocket(game.getId(), game);
            }
            else if (game.getGameStatus().equals("SHUTDOWN")) {
                gameMap.remove(game.getId());
            }
        }
    }

    public void updateGamerActivity(String username) {
        // 기본 접속 시간을 현재 시간으로 갱신
        GamePlayer gamePlayer = gamerMap.get(gamerService.findByUserName(username).getId());
        gamePlayer.setDateTime(LocalDateTime.now().toString());
        gamerMap.put(gamerService.findByUserName(username).getId(), gamePlayer);
    }
}



