package com.mafia.game.server.game.gameDto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameLeaveDTO {
    private Long gameId;
    private String userName;

    public GameLeaveDTO(Long id, String username) {
        this.gameId = id;
        this.userName = username;
    }
}
