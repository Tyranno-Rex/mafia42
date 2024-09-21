package com.mafia.game.server.game.gameStatus;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameUser {

    private String username;
    private String role;
    private boolean alive;

    public GameUser(String username, String role, boolean alive) {
        this.username = username;
        this.role = role;
        this.alive = alive;
    }
}
