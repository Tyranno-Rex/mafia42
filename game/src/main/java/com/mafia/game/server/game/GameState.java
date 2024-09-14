package com.mafia.game.server.game;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.server.game.gameDto.GameSocketDTO;
import com.mafia.game.server.game.gameDto.GamerSocketDTO;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.springframework.data.repository.cdi.Eager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private int playerCount;
    private int mafiaCount;
    private int policeCount;
    private int doctorCount;
    private int citizenCount;

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
        dto.setPlayerCount(this.playerCount);
        dto.setMafiaCount(this.mafiaCount);
        dto.setPoliceCount(this.policeCount);
        dto.setDoctorCount(this.doctorCount);
        dto.setCitizenCount(this.citizenCount);

        for (Gamer gamer : this.players) {
            GamerSocketDTO gamerDTO = new GamerSocketDTO();
            gamerDTO.setId(gamer.getId());
            gamerDTO.setUserName(gamer.getUserName());
            dto.getPlayers().add(gamerDTO);
        }
        return dto;
    }
}
