package com.mafia.game.server.game.gameStatus;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.server.game.gameDto.GameSocketDTO;
import com.mafia.game.server.game.gameDto.GamerSocketDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GameState {
    private Long id;
    private String gameName;
    private String gamePassword;
    private String gameStatus;
    private String gameOwner;
    private int gamePlayerCount;
    private int gameMaxPlayerCount;
    private String Datetime;
    private String message;

    private int phaseTime;
    private int phaseTimeMax;
    private String phaseStep;
    private List<GamePlayer> gamePlayers = new ArrayList<>();

    private String playerDoctorSaved;
    private String playerMafiaKill;
    private String playerSelectedByVote;

    private int playerCount;
    private int mafiaCount;
    private int policeCount;
    private int doctorCount;
    private int citizenCount;

    // ActionMap is used to store the actions of the players
    // <username, action>
    private Map<String, String> ActionMap = new HashMap<>();

    // Eager fetch is used to load all the players at once
    private List<Gamer> players = new ArrayList<>();

    public GameState() {
    }

    public GameState(Long gameid, String gameName, String gamePassword,
                     String gameStatus, String gameOwner,
                     int gamePlayerCount, int gameMaxPlayerCount) {
        this.id = gameid;
        this.gameName = gameName;
        this.gamePassword = gamePassword;
        this.gameStatus = gameStatus;
        this.gameOwner = gameOwner;
        this.gamePlayerCount = gamePlayerCount;
        this.gameMaxPlayerCount = gameMaxPlayerCount;
        this.Datetime = LocalDateTime.now().toString();
    }

    public GameSocketDTO toDTO() {
        GameSocketDTO dto = new GameSocketDTO();
        dto.setId(this.id);
        dto.setGameName(this.gameName);
        dto.setGamePassword(this.gamePassword);
        dto.setGameStatus(this.gameStatus);
        dto.setGameOwner(this.gameOwner);
        dto.setGamePlayerCount(this.gamePlayerCount);
        dto.setGameMaxPlayerCount(this.gameMaxPlayerCount);
        dto.setDatetime(this.Datetime);
        dto.setMessage(this.message);
        dto.setPhaseTime(this.phaseTime);
        dto.setPhaseTimeMax(this.phaseTimeMax);
        dto.setPhaseStep(this.phaseStep);
        dto.setPlayerDoctorSaved(this.playerDoctorSaved);
        dto.setPlayerMafiaKill(this.playerMafiaKill);
        dto.setPlayerCount(this.playerCount);
        dto.setMafiaCount(this.mafiaCount);
        dto.setPoliceCount(this.policeCount);
        dto.setDoctorCount(this.doctorCount);
        dto.setCitizenCount(this.citizenCount);

        for (GamePlayer gamer : this.gamePlayers) {
            GamerSocketDTO gamerDTO = new GamerSocketDTO();
            gamerDTO.setUserName(gamer.getUsername());
            gamerDTO.setIsReady(gamer.getIsReady());
            gamerDTO.setIsAlive(gamer.getIsAlive());
            dto.getPlayers().add(gamerDTO);
        }
        return dto;
    }
}
