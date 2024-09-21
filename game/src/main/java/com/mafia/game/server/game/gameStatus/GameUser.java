package com.mafia.game.server.game.gameStatus;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameUser {

    private String username;
    private String role;
    private String message;
    private boolean alive;

    public GameUser(String username, String role, String message, boolean alive) {
        this.username = username;
        this.role = role;
        this.message = message;
        this.alive = alive;
    }
}
