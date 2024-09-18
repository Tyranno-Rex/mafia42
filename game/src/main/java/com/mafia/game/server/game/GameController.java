package com.mafia.game.server.game;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.model.gamer.GamerService;
import com.mafia.game.server.game.gameDto.GameDeleteDTO;
import com.mafia.game.server.game.gameDto.GameJoinDTO;
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
    Map<Long, String> gamerMap = new HashMap<>(); // 게이머의 접속 시간을 기록

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
            // gamerMap에 게이머의 접속 시간을 기록
            gamerMap.put(newPlayer.getId(), LocalDateTime.now().toString());
            System.out.println("GameMap Size: " + gameMap.size());
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
        System.out.println("GameMap Size: " + gameMap.size());
        System.out.println("GamerMap Size: " + gamerMap.size());


        for (Game game : gameService.getAllGames()){
            if (!gameMap.containsKey(game.getId())) {
                game.setGameStatus("SHUTDOWN");
                gameService.saveGame(game);
            }
        }

        Iterator<Map.Entry<Long, String>> gamerMapIterator = gamerMap.entrySet().iterator();
        while (gamerMapIterator.hasNext()) {
            Map.Entry<Long, String> entry = gamerMapIterator.next();
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(entry.getValue()).plusSeconds(5))) {
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
            if (game.getGameStatus().equals("CREATED")) {
                continue;
            }
            if (game.getGameStatus().equals("WAITING") || game.getGameStatus().equals("PLAYING")) {
                List<Gamer> players = game.getPlayers();
                if (players.isEmpty()) {
                    gameMap.remove(game.getId());
                } else {



                    socketController.GameSocket(game.getId(), game);
                }
            }
        }
    }

    public void updateGamerActivity(String username) {
        // 기본 접속 시간을 현재 시간으로 갱신
        gamerMap.put(gamerService.findByUserName(username).getId(), LocalDateTime.now().toString());
    }
}



