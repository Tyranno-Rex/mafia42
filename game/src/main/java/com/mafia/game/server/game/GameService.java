package com.mafia.game.server.game;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.model.gamer.GamerService;
import com.mafia.game.server.game.gameDto.GameJoinDTO;
import com.mafia.game.server.game.gameDto.GameDeleteDTO;
import com.mafia.game.server.game.gameDto.GameStatusDTO;
import com.mafia.game.server.game.gameStatus.GamePlayer;
import com.mafia.game.server.game.gameStatus.GameState;
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
        Game game = gameRepository.findById(gamejoinDTO.getGameId()).orElseThrow();
        Gamer gamer = gamerService.findByUserName(gamejoinDTO.getUserName());
        if (game.getPlayers().contains(gamer)) {
            return;
        }
        System.out.println("User joined the game: " + gamer.getUserName());
        game.addPlayer(gamer);
        gameRepository.save(game);
    }

    public List<Gamer> getUsers(Long roomId) {
        Game game = gameRepository.findById(roomId).orElseThrow();
        return game.getPlayers();
    }

    // 마피아 2명 경찰 1명 의사 1명 시민 4명
    public GameState SetGameState(GameState gameState) {
        gameState.setPhaseTime(60);
        gameState.setPhaseStep("NIGHT");

        // 랜덤으로 마피아, 경찰, 의사, 시민을 배정
        List<Gamer> players = gameState.getPlayers();
        List<GamePlayer> gamePlayers = gameState.getGamePlayers();
        for (int i = 0; i < 8; i++) {
            int random = (int) (Math.random() * players.size());
            if (i < 2) {
                gamePlayers.add(new GamePlayer(players.get(random).getUserName(), "MAFIA", true, true));
            } else if (i < 3) {
                gamePlayers.add(new GamePlayer(players.get(random).getUserName(), "POLICE", true, true));
            } else if (i < 4) {
                gamePlayers.add(new GamePlayer(players.get(random).getUserName(), "DOCTOR", true, true));
            } else {
                gamePlayers.add(new GamePlayer(players.get(random).getUserName(), "CITIZEN", true, true));
            }
            players.remove(random);
        }
        gameState.setGamePlayers(gamePlayers);
        return gameState;
    }

    // Night    : 마피아는 죽일 사람을 선택, 의사는 살릴 사람을 선택, 경찰은 조사할 사람을 선택
    // Check1   : 게임 종료 조건 확인
    // Day      : 마피아가 죽일 사람을 투표, 시민들이 마피아로 의심되는 사람을 투표
    // Vote1    : 투표로 죽일 사람을 선택
    // Vote2    : 투표로 선택된 사람을 죽일지 말지 결정
    // Check2   : 게임 종료 조건 확인
    public GameState updateGameState(GameState gameState) {
        if (gameState.getPhaseStep().equals("NIGHT")) {
            if (gameState.getPhaseTime() < 0){

            }

        }



        gameState.setPhaseTime(gameState.getPhaseTime() - 1);
        return gameState;
    }
}
