package com.mafia.game.server.game;

import com.mafia.game.server.game.gameDto.GameDeleteDTO;
import com.mafia.game.server.game.gameDto.GameJoinDTO;
import com.mafia.game.server.socket.SocketController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.mafia.game.server.game.gameDto.GameStatusDTO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;
    private final SocketController socketController;

    @GetMapping("/all")
    public Iterable<Game> getAllGames() {
        return gameService.getAllGames();
    }

    @PostMapping("/create")
    public Map<String, String> createGame(@RequestBody GameStatusDTO gameStatusDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            gameService.createGame(gameStatusDTO);
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
            System.out.println("JOIN Game: " + gameJoinDTO.getGameId() + " " + gameJoinDTO.getUserName());
            gameService.joinGame(gameJoinDTO);
            System.out.println("Check 1");
//            socketController.userJoinSocket(gameJoinDTO.getGameId(), gameService.getUsers(gameJoinDTO.getGameId()));
            System.out.println("Check 2");
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            response.put("status", "fail");
            return response;
        }
    }
}
