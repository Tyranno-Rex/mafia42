package com.mafia.game.server.game.gameDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameReadyDTO {
    private Long gameId;
    private String username;
    private Boolean readyStatus;
}
