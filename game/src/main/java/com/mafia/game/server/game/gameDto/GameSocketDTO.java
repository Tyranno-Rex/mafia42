package com.mafia.game.server.game.gameDto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameSocketDTO {

    private Long id;
    private String gameName;
    private String gamePassword;
    private String gameStatus;
    private String gameOwner;
    private int gamePlayerCount;
    private int gameMaxPlayerCount;
    private String datetime;

    private int phaseTime;
    private int phaseTimeMax;
    private String phaseStep;

    private int playerCount;
    private int mafiaCount;
    private int policeCount;
    private int doctorCount;
    private int citizenCount;

    private List<GamerSocketDTO> players = new ArrayList<>();
}
