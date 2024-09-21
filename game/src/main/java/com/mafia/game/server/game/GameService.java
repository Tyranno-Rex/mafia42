package com.mafia.game.server.game;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.model.gamer.GamerService;
import com.mafia.game.server.game.gameDto.GameJoinDTO;
import com.mafia.game.server.game.gameDto.GameDeleteDTO;
import com.mafia.game.server.game.gameDto.GameStatusDTO;
import com.mafia.game.server.game.gameStatus.GamePlayer;
import com.mafia.game.server.game.gameStatus.GameState;
import com.mafia.game.server.socket.SocketController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GamerService gamerService;
    private final SocketController socketController;


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

        List<GamePlayer> gamePlayers = new ArrayList<>();
        List<GamePlayer> copyPlayers = new ArrayList<>(gameState.getGamePlayers());
        for (int i = 0; i < 4; i++) {
            int random = (int) (Math.random() * copyPlayers.size());
            String username = copyPlayers.get(random).getUsername();
            if (i < 1) {
                gamePlayers.add(new GamePlayer(username, "MAFIA", true, true));
            } else if (i < 2) {
                gamePlayers.add(new GamePlayer(username, "POLICE", true, true));
            } else if (i < 3) {
                gamePlayers.add(new GamePlayer(username, "DOCTOR", true, true));
            } else {
                gamePlayers.add(new GamePlayer(username, "CITIZEN", true, true));
            }
            copyPlayers.remove(random);
        }
        gameState.setGamePlayers(gamePlayers);
        Game game = gameRepository.findById(gameState.getId()).orElseThrow();
        game.setPlayerRoles(
                1, gamePlayers.get(0).getUsername(),
                1, gamePlayers.get(1).getUsername(),
                1, gamePlayers.get(2).getUsername(),
                1, gamePlayers.get(3).getUsername()
        );
        return gameState;
    }

    // Night            : 마피아는 죽일 사람을 선택, 의사는 살릴 사람을 선택, 경찰은 조사할 사람을 선택 (30초)
    // Check1           : 게임 종료 조건 확인 (1초)
    // Day              : 시민들이 마피아로 의심되는 사람을 토론함 (60초) (빠른 진행을 위해 30초로 지정)
    // Vote1            : 투표로 죽일 사람을 선택 (10초)
    // Final remarks    : 최후의 변론 (30초)
    // Vote2            : 투표로 선택된 사람을 죽일지 말지 결정 (10초)
    // Check2           : 게임 종료 조건 확인
    public GameState updateGameState(GameState gameState) throws Exception {
        System.out.println("updateGameState: " + gameState.getPhaseStep() + " " + gameState.getPhaseTime());
        String StartTime = LocalDateTime.now().toString();
        if (gameState.getGamePlayers().isEmpty()) {
            return gameState;
        }
        if (gameState.getPhaseStep().equals("NIGHT")) {
            gameState.setPlayerDoctorSaved("");
            gameState.setPlayerMafiaKill("");

            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("CHECK1");
                gameState.setPhaseTime(1);
                gameState.setPhaseTimeMax(1);
            }
        }
        if (gameState.getPhaseStep().equals("CHECK1")) {
            String mafia_kill = "";
            String doctor_save = "";
            String police_check = "";

            if (gameState.getPhaseTime() < 0){
                gameState.setPhaseStep("DAY");
                gameState.setPhaseTime(30);
                gameState.setPhaseTimeMax(30);

                Map<String, String> actionMap = gameState.getActionMap();

                for (String username : actionMap.keySet()) {
                    String action = actionMap.get(username);
                    if (action.startsWith("mafia")) {
                        // username이 마피아일 때
                        if (gameState.getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getUsername().equals(username)).findFirst().get().getRole().equals("MAFIA")) {
                            mafia_kill = action.split("/")[1];
                        }
                    }
                    if (action.startsWith("doctor")) {
                        if (gameState.getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getUsername().equals(username)).findFirst().get().getRole().equals("DOCTOR")) {
                            doctor_save = action.split("/")[1];
                        }
                    }
                    if (action.startsWith("police")) {
                        if (gameState.getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getUsername().equals(username)).findFirst().get().getRole().equals("POLICE")) {
                            police_check = action.split("/")[1];
                        }
                    }
                }

                List<GamePlayer> gamePlayers = gameState.getGamePlayers();

                for (GamePlayer gamePlayer : gamePlayers) {
                    if (gamePlayer.getUsername().equals(mafia_kill)) {
                        gamePlayer.setIsAlive(false);
                        gameState.setPlayerMafiaKill(mafia_kill);
                        System.out.println("mafia_kill: " + mafia_kill);
                    }
                }

                gameState.setPlayerDoctorSaved(doctor_save);
                for (GamePlayer gamePlayer : gamePlayers) {
                    if (gamePlayer.getUsername().equals(doctor_save) && doctor_save.equals(mafia_kill)) {
                        gamePlayer.setIsAlive(true);
                        System.out.println("doctor_save: " + doctor_save);
                    }
                }

                String police_name = gameState.getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getRole().equals("POLICE")).findFirst().get().getUsername();
                for (GamePlayer gamePlayer : gamePlayers) {
                    if (gamePlayer.getUsername().equals(police_check)) {
                        if (gamePlayer.getRole().equals("MAFIA")) {
                            System.out.println("police find mafia: " + police_check);
                            socketController.UserSocket(gameState.getId(), police_name, gameState, "마피아입니다.");
                        } else {
                            System.out.println("police find citizen: " + police_check);
                            socketController.UserSocket(gameState.getId(), police_name, gameState, "시민입니다.");
                        }
                    }
                }
                gameState.setGamePlayers(gamePlayers);
                gameState.setActionMap(new HashMap<>());
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
                gameState.setPhaseTime(10);
                gameState.setPhaseTimeMax(10);


                Map<String, String> actionMap = gameState.getActionMap();
                List<String> SelectedPlayer = new ArrayList<>();
                int voteCount = 0;
                String voteSelectedPlayer = "";

                for (String username : actionMap.keySet()) {
                    String action = actionMap.get(username);
                    if (action.startsWith("vote1")) {
                        voteCount++;
                        voteSelectedPlayer = action.split("/")[1];
                        SelectedPlayer.add(voteSelectedPlayer);
                    }
                }

                int max = 0;
                String maxPlayer = "";
                for (String player : SelectedPlayer) {
                    int count = 0;
                    for (String player2 : SelectedPlayer) {
                        if (player.equals(player2)) {
                            count++;
                        }
                    }
                    if (count > max) {
                        max = count;
                        maxPlayer = player;
                    }
                }

                if (max >= voteCount / 2) {
                    gameState.setMessage("투표로 " + maxPlayer + "님이 선택되었습니다.");
                    gameState.setPlayerSelectedByVote(maxPlayer);
                    socketController.GameSocket(gameState.getId(), gameState);
                    gameState.setMessage("");
                } else {
                    gameState.setMessage("투표로 아무도 선택되지 않았습니다.");
                    socketController.GameSocket(gameState.getId(), gameState);
                    gameState.setMessage("");
                }
                gameState.setActionMap(new HashMap<>());
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

                Map<String, String> actionMap = gameState.getActionMap();

                List<String> SelectedPlayer = new ArrayList<>();
                int voteCount = 0;
                String AgreeDisagree = ""; //

                for (String username : actionMap.keySet()) {
                    String action = actionMap.get(username);
                    if (action.startsWith("vote2")) {
                        voteCount++;
                        AgreeDisagree = action.split("/")[1];
                        SelectedPlayer.add(AgreeDisagree);
                    }
                }

                int Agree = 0;
                int Disagree = 0;

                for (String player : SelectedPlayer) {
                    if (player.equals("agree")) {
                        Agree++;
                    } else {
                        Disagree++;
                    }
                }

                if (Agree > Disagree) {
                    gameState.setMessage("투표로 인해 " + gameState.getPlayerSelectedByVote() + "님이 죽었습니다.");
                    gameState.getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getUsername().equals(gameState.getPlayerSelectedByVote())).findFirst().get().setIsAlive(false);
                    socketController.GameSocket(gameState.getId(), gameState);
                    gameState.setMessage("");
                } else {

                    socketController.GameSocket(gameState.getId(), gameState);
                    gameState.setMessage("");
                }


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
