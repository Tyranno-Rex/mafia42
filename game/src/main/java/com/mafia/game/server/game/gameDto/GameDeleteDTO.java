package com.mafia.game.server.game.gameDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameDeleteDTO {
    private Long gameId;
    private String roomName;
    private String userName;
}
