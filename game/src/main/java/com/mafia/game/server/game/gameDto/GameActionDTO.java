package com.mafia.game.server.game.gameDto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameActionDTO {
    private Long gameId;
    private String username;
    private String action;
}
