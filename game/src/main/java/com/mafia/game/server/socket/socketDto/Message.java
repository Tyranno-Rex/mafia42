package com.mafia.game.server.socket.socketDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Message {
    private String content;
    private String uuid;
    private String gameId;
}
