package com.mafia.game.server.game;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.model.gamer.GamerService;
import com.mafia.game.server.game.gameDto.*;
import com.mafia.game.server.game.gameEvent.GamerActivityEvent;
import com.mafia.game.server.game.gameEvent.GamerActionEvent;
import com.mafia.game.server.game.gameStatus.GamePlayer;
import com.mafia.game.server.game.gameStatus.GameState;
import com.mafia.game.server.socket.SocketController;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

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

    @EventListener
    public void handlePlayerActionEvent(GamerActionEvent event) {
        applyPlayerAction(event.getGameId(), event.getUsername(), event.getAction(), event.getTarget());
    }

    @GetMapping("/all")
    public Map<Long, GameState> getAllGames() {
//        List<Game> games = gameService.getActiveGames();
//        Map<Long, GameState> reponse_gameMap = new HashMap<>();
//        for (Game game : games) {
//            reponse_gameMap.put(game.getId(), new GameState(game.getId(),
//                    game.getRoomName(), game.getRoomPassword(),
//                    game.getRoomStatus(), game.getRoomOwner(),
//                    game.getRoomPlayerCount(), game.getRoomMaxPlayerCount()));
//        }

        Map<Long, GameState> reponse_gameMap = new HashMap<>();
        for (GameState game : gameMap.values()) {
            reponse_gameMap.put(game.getId(), game);
        }

        return reponse_gameMap;
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

    @PostMapping("/leave")
    @Transactional
    public Map<String, String> leaveGame(@RequestBody GameLeaveDTO gameLeaveDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            // 실제 DB에서 게임 정보를 업데이트
            gameService.deleteUser(gameLeaveDTO.getGameId(), gameLeaveDTO.getUserName());
            // gameMap에서 게임 정보를 업데이트
            GameState game = gameMap.get(gameLeaveDTO.getGameId());
            List<Gamer> players = new ArrayList<>(game.getPlayers());
            players.removeIf(p -> p.getUserName().equals(gameLeaveDTO.getUserName()));
            game.setPlayers(players);
            gameMap.put(game.getId(), game);
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

            List<GamePlayer> gamePlayers = new ArrayList<>(game.getGamePlayers());
            gamePlayers.add(newGamePlayer);
            game.setGamePlayers(gamePlayers);
            gameMap.put(game.getId(), game);

            System.out.println("Gamer is added to the game: " + newPlayer.getUserName());
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
            List<GamePlayer> gamePlayers = game.getGamePlayers();
            for (GamePlayer gamePlayer : gamePlayers) {
                if (gamePlayer.getUsername().equals(gameReadyDTO.getUsername())) {
                    gamePlayer.setIsReady(gameReadyDTO.getReadyStatus());
                }
            }
            game.setGamePlayers(gamePlayers);
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
                    leaveGame(new GameLeaveDTO(game.getId(), entry.getValue().getUsername()));
                }
            }
        }

        for (GameState game : gameMap.values()) {
            System.out.println("Game ID: " + game.getId() + " Game Status: " + game.getGameStatus() + " Player Count: " + game.getPlayers().size());
            for (GamePlayer gamePlayer : game.getGamePlayers()) {
                System.out.println("Player: " + gamePlayer.getUsername() + " Role: " + gamePlayer.getRole() + " Alive: " + gamePlayer.getIsAlive() + " Ready: " + gamePlayer.getIsReady());
            }
            switch (game.getGameStatus()) {
                case "WAITING" -> {
                    List<Gamer> players = game.getPlayers();
                    List<GamePlayer> gamePlayers = game.getGamePlayers();
                    if (players.isEmpty())
                        gameMap.remove(game.getId());
                    else if (players.size() == game.getGameMaxPlayerCount()) {
                        boolean isAllReady = true;
                        for (GamePlayer gamePlayer : gamePlayers) {
                            if (!gamePlayer.getIsReady()) {
                                isAllReady = false;
                                break;
                            }
                        }
                        if (isAllReady) {
                            game.setGameStatus("STARTING");
                            gameMap.put(game.getId(), game);
                        }
                        socketController.GameSocket(game.getId(), game);
                    } else
                        socketController.GameSocket(game.getId(), game);
                }
                case "STARTING" -> {
                    game.setGameStatus("PLAYING");

                    // 게임 시작 시 마피아, 경찰, 의사, 시민을 배정
                    if (game.getPlayers().size() == 4) {
                        game = gameService.SetGame4State(game);
                    } else
                        continue;

                    // 디버깅용
                    List<GamePlayer> gamePlayers = game.getGamePlayers();
                    for (int i = 0; i < 4; i++) {
                        System.out.println("Player: " + gamePlayers.get(i).getUsername());
                        System.out.println("Role: " + gamePlayers.get(i).getRole());
                    }
                    // 게임 상태 저장
                    gameMap.put(game.getId(), game);
                    for (Gamer player : game.getPlayers()) {
                        socketController.UserSocket(game.getId(), player.getUserName(), game, "ROLE");
                    }
                }
                case "PLAYING" -> {
                    if (game.getPlayers().size() < 4) {
                        game.setGameStatus("SHUTDOWN");
                        gameMap.put(game.getId(), game);
                        continue;
                    }
                    game = gameService.updateGameState(game);
                    gameMap.put(game.getId(), game);
                    socketController.GameSocket(game.getId(), game);
                    game.setPlayerDoctorSaved("");
                    game.setPlayerMafiaKill("");
                }
                case "SHUTDOWN" -> gameMap.remove(game.getId());
            }
        }
    }

    public void updateGamerActivity(String username) {
        // 기본 접속 시간을 현재 시간으로 갱신
        GamePlayer gamePlayer = gamerMap.get(gamerService.findByUserName(username).getId());
        gamePlayer.setDateTime(LocalDateTime.now().toString());
        gamerMap.put(gamerService.findByUserName(username).getId(), gamePlayer);
    }

    public void applyPlayerAction(Long gameId, String username, String action, String target) {
        GameState game = gameMap.get(gameId);

        if (action.equals("mafia") || action.equals("police") || action.equals("doctor")){
            if (game.getPhaseStep().equals("NIGHT")) {
                switch (action) {
                    case "mafia":
                        if (game.getGamePlayers().stream().anyMatch(p -> p.getUsername().equals(username) && p.getRole().equals("MAFIA")))
                            game.getActionMap().put(username, action + "/" + target);
                        break;
                    case "police":
                        if (game.getGamePlayers().stream().anyMatch(p -> p.getUsername().equals(username) && p.getRole().equals("POLICE")))
                            game.getActionMap().put(username, action + "/" + target);
                        break;
                    case "doctor":
                        if (game.getGamePlayers().stream().anyMatch(p -> p.getUsername().equals(username) && p.getRole().equals("DOCTOR")))
                            game.getActionMap().put(username, action + "/" + target);
                        break;
                }
            }
        }

        if (action.equals("vote1")) {
            if (game.getPhaseStep().equals("VOTE1")) {
                game.getActionMap().put(username, action + "/" + target);
            }
        }

        if (action.equals("vote2")) {
            if (game.getPhaseStep().equals("VOTE2")) {
                game.getActionMap().put(username, action + "/" + target);
            }
        }
    }
}



