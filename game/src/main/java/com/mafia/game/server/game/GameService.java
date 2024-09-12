package com.mafia.game.server.game;

import com.mafia.game.server.game.gameDto.GameStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game createGame(GameStatusDTO gameStatusDTO) {
        Game game = new Game(
                gameStatusDTO.getRoomName(), gameStatusDTO.getRoomPassword(),
                gameStatusDTO.getRoomStatus(), gameStatusDTO.getRoomOwner(),
                gameStatusDTO.getRoomPlayerCount(), gameStatusDTO.getRoomMaxPlayerCount());
        gameRepository.save(game);
        return game;
    }

}
