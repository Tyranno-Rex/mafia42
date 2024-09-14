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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;
    private final SocketController socketController;
    private final GamerService gamerService;
    Map<Long, Game> gameMap = new HashMap<>(); // 게임 전체의 정보를 기록
    Map<Long, String> gamerMap = new HashMap<>(); // 게이머의 접속 시간을 기록

    @EventListener
    public void handleGamerActivityEvent(GamerActivityEvent event) {
        updateGamerActivity(event.getUsername());
    }

    @GetMapping("/all")
    public Iterable<Game> getAllGames() {
        return gameService.getAllGames();
    }

    @PostMapping("/create")
    public Map<String, String> createGame(@RequestBody GameStatusDTO gameStatusDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            Game game = gameService.createGame(gameStatusDTO);
            Long gameId = game.getId();
            gameMap.put(gameId, game);

            response.put("status", "success");
            return response;
        } catch (Exception e) {
            response.put("status", "fail");
            return response;
        }
    }

    @DeleteMapping("/delete")
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
    public Map<String, String> joinGame(@RequestBody GameJoinDTO gameJoinDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            gameService.joinGame(gameJoinDTO);
            Game game = gameMap.get(gameJoinDTO.getGameId());
            game.addPlayer(gamerService.findByUserName(gameJoinDTO.getUserName()));
            gameMap.put(gameJoinDTO.getGameId(), game);

            Gamer gamer = gamerService.findByUserName(gameJoinDTO.getUserName());
            gamerMap.put(gamer.getId(), LocalDateTime.now().toString());

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
        for (Game game : gameMap.values()) {
            if (game.getRoomStatus().equals("CREATED")) {
                continue;
            }
            if (game.getRoomStatus().equals("WAITING") || game.getRoomStatus().equals("PLAYING")) {
                List<Gamer> players = game.getPlayers();
                if (players.isEmpty()) {
                    GameDeleteDTO gameDeleteDTO = new GameDeleteDTO();
                    gameDeleteDTO.setGameId(game.getId());
                    gameDeleteDTO.setRoomName(game.getRoomName());
                    gameDeleteDTO.setUserName(game.getRoomOwner());
                    gameService.deleteGame(gameDeleteDTO);
                    gameMap.remove(game.getId());
                } else {
                    // 게이머의 접속 시간을 확인하여 5초 이상 접속이 없으면 게임에서 제외
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



