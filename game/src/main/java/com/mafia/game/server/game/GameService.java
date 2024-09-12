package com.mafia.game.server.game;

import com.mafia.game.server.game.gameDto.GameJoinDTO;
import com.mafia.game.server.game.gameDto.GameDeleteDTO;
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
                gameStatusDTO.getGameName(), gameStatusDTO.getGamePassword(),
                gameStatusDTO.getGameStatus(), gameStatusDTO.getGameOwner(),
                gameStatusDTO.getGamePlayerCount(), gameStatusDTO.getGameMaxPlayerCount());
        gameRepository.save(game);
        return game;
    }

    public void deleteGame(GameDeleteDTO gameDeleteDTO) {
        Game game = gameRepository.findById(gameDeleteDTO.getGameId()).orElseThrow();
        if (game.getRoomOwner().equals(gameDeleteDTO.getUserName())) {
            gameRepository.deleteById(gameDeleteDTO.getGameId());
        }
    }

    public void joinGame(GameJoinDTO gamejoinDTO) {
        System.out.println("DTO: " + gamejoinDTO.getGameId() + " " + gamejoinDTO.getUserName());
        Game game = gameRepository.findById(gamejoinDTO.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gamejoinDTO.getGameId()));
//        game.addPlayer(gamejoinDTO.getUserName());
        System.out.println("GET Player: " + game.getPlayers());
        gameRepository.save(game);
    }

//    public List<String> getUsers(Long roomId) {
//        Game game = gameRepository.findById(roomId).orElseThrow();
//        return game.getPlayers();
//    }
}
