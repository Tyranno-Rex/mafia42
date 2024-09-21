package com.mafia.game.server.game.gameEvent;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerActionEvent {
    private final Long gameId;
    private final String username;
    private final String action;
    private final String target;
    private final String ProCon;

    public GamerActionEvent(Long gameId, String username, String action, String target, String ProCon) {
        this.gameId = gameId;
        this.username = username;
        this.action = action;
        this.target = target;
        this.ProCon = ProCon;
    }
}
