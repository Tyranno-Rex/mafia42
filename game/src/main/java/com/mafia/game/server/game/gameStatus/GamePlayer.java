package com.mafia.game.server.game.gameStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamePlayer {
    private String username;
    private String role;
    private String DateTime;
    private boolean isAlive;
    private boolean isReady;

    public GamePlayer() {
    }

    public GamePlayer(String username, String role, boolean isAlive, boolean isReady) {
        this.username = username;
        this.role = role;
        this.isAlive = isAlive;
        this.isReady = isReady;
    }
}
