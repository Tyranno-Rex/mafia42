package com.mafia.game.server.socket.socketDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckMessage {
    private String content;
    private String username;
    private Long gameId;
}
