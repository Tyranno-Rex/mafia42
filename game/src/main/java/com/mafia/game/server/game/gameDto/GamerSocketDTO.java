package com.mafia.game.server.game.gameDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerSocketDTO {
    private String userName;
    private Boolean isReady;
    private Boolean isAlive;
}