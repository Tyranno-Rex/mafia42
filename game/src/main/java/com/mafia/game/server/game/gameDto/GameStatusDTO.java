package com.mafia.game.server.game.gameDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameStatusDTO {
    private String gameName;
    private String gamePassword;
    private String gameStatus;
    private String gameOwner;
    private int gamePlayerCount;
    private int gameMaxPlayerCount;
}
