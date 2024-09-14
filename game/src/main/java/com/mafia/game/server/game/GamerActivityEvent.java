package com.mafia.game.server.game;

import org.springframework.context.ApplicationEvent;

public class GamerActivityEvent extends ApplicationEvent {
    private final String username;

    public GamerActivityEvent(String username) {
        super(username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}