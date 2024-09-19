package com.mafia.game.server.game.gameDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerSocketDTO {
    private Long id;
    private String userName;
    private Boolean isReady;
}