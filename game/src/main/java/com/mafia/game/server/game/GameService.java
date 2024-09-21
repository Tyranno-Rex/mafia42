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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // 마피아 1명 경찰 1명 의사 1명 시민 1명으로 게임 시작
    public GameState SetGame4State(GameState gameState) {
        gameState.setPhaseTime(30);
        gameState.setPhaseTimeMax(30);
        gameState.setPhaseStep("NIGHT");

        // 랜덤으로 마피아, 경찰, 의사, 시민을 배정
        List<Gamer> players = gameState.getPlayers();
        List<Gamer> copyPlayers = new ArrayList<>(players);
        List<GamePlayer> gamePlayers = gameState.getGamePlayers();
        for (int i = 0; i < 4; i++) {
            int random = (int) (Math.random() * copyPlayers.size());
            if (i < 1) {
                gamePlayers.add(new GamePlayer(copyPlayers.get(random).getUserName(), "MAFIA", true, true));
            } else if (i < 2) {
                gamePlayers.add(new GamePlayer(copyPlayers.get(random).getUserName(), "POLICE", true, true));
            } else if (i < 3) {
                gamePlayers.add(new GamePlayer(copyPlayers.get(random).getUserName(), "DOCTOR", true, true));
            } else {
                gamePlayers.add(new GamePlayer(copyPlayers.get(random).getUserName(), "CITIZEN", true, true));
            }
            copyPlayers.remove(random);
        }

        Game game = gameRepository.findById(gameState.getId()).orElseThrow();
        game.setPlayerRoles(1, gamePlayers.get(0).getUsername(), 1, gamePlayers.get(1).getUsername(), 1, gamePlayers.get(2).getUsername(), 1, gamePlayers.get(3).getUsername());

        gameState.setGamePlayers(gamePlayers);
        return gameState;
    }

    // Night            : 마피아는 죽일 사람을 선택, 의사는 살릴 사람을 선택, 경찰은 조사할 사람을 선택 (30초)
    // Check1           : 게임 종료 조건 확인 (1초)
    // Day              : 시민들이 마피아로 의심되는 사람을 토론함 (60초)
    // Vote1            : 투표로 죽일 사람을 선택 (10초)
    // Final remarks    : 최후의 변론 (30초)
    // Vote2            : 투표로 선택된 사람을 죽일지 말지 결정 (10초)
    // Check2           : 게임 종료 조건 확인
    public GameState updateGameState(GameState gameState) {
        System.out.println("updateGameState: " + gameState.getPhaseStep() + " " + gameState.getPhaseTime());
        if (gameState.getGamePlayers().isEmpty()) {
            return gameState;
        }
        if (gameState.getPhaseStep().equals("NIGHT")) {
            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("CHECK1");
                gameState.setPhaseTime(1);
                gameState.setPhaseTimeMax(1);
            }
        }
        if (gameState.getPhaseStep().equals("CHECK1")) {
            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("DAY");
                Map<String, String> actionMap = gameState.getActionMap();
                for (String username : actionMap.keySet()) {
                    String action = actionMap.get(username);
                    System.out.println("Action: " + username + " " + action);
                }
                gameState.setPhaseTime(60);
                gameState.setPhaseTimeMax(60);
            }
        }
        if (gameState.getPhaseStep().equals("DAY")) {
            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("VOTE1");
                gameState.setPhaseTime(10);
                gameState.setPhaseTimeMax(10);
            }
        }
        if (gameState.getPhaseStep().equals("VOTE1")) {
            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("FINAL_REMARKS");
                gameState.setPhaseTime(30);
                gameState.setPhaseTimeMax(30);
            }
        }
        if (gameState.getPhaseStep().equals("FINAL_REMARKS")) {
            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("VOTE2");
                gameState.setPhaseTime(10);
                gameState.setPhaseTimeMax(10);
            }
        }
        if (gameState.getPhaseStep().equals("VOTE2")) {
            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("CHECK2");
                gameState.setPhaseTime(1);
                gameState.setPhaseTimeMax(1);
            }
        }
        if (gameState.getPhaseStep().equals("CHECK2")) {
            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("NIGHT");
                gameState.setPhaseTime(30);
                gameState.setPhaseTimeMax(30);
            }
        }
        gameState.setPhaseTime(gameState.getPhaseTime() - 1);
        return gameState;
    }
}
