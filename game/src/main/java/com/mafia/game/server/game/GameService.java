package com.mafia.game.server.game;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.model.gamer.GamerService;
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
    private final GamerService gamerService;


    // GameState가 Shutdown인 게임을 제외한 모든 게임을 반환
    public List<Game> getActiveGames() {
        return gameRepository.findByGameStatusNot();
    }

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

    public void saveGame(Game game) {
        gameRepository.save(game);
    }

    public void deleteGame(GameDeleteDTO gameDeleteDTO) {
        Game game = gameRepository.findById(gameDeleteDTO.getGameId()).orElseThrow();
        if (game.getRoomOwner().equals(gameDeleteDTO.getUserName())) {
            gameRepository.deleteById(gameDeleteDTO.getGameId());
        }
    }

    public void deleteUser(Long roomId, String userName) {
        Game game = gameRepository.findById(roomId).orElseThrow();
        game.removePlayer(userName);
        gameRepository.save(game);
    }

    public void joinGame(GameJoinDTO gamejoinDTO) {
        Game game = gameRepository.findById(gamejoinDTO.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gamejoinDTO.getGameId()));
        Gamer gamer = gamerService.findByUserName(gamejoinDTO.getUserName());
        if (game.getPlayers().contains(gamer)) {
            throw new IllegalArgumentException("User already joined the game");
        }
        game.addPlayer(gamer);
        gameRepository.save(game);
    }

    public List<Gamer> getUsers(Long roomId) {
        Game game = gameRepository.findById(roomId).orElseThrow();
        return game.getPlayers();
    }
}
