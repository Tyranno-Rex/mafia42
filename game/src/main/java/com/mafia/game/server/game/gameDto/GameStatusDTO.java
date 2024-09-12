package com.mafia.game.server.game.gameDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameStatusDTO {
    private String roomName;
    private String roomPassword;
    private String roomStatus;
    private String roomOwner;
    private String roomPlayerCount;
    private String roomMaxPlayerCount;
}
