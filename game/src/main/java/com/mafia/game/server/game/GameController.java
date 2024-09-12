package com.mafia.game.server.game;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.mafia.game.server.game.gameDto.GameStatusDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    @GetMapping("/all")
    public Iterable<Game> getAllGames() {
        return gameService.getAllGames();
    }

    @PostMapping("/create")
    public Game createGame(@RequestBody GameStatusDTO gameStatusDTO) {
        return gameService.createGame(gameStatusDTO);
    }
}
