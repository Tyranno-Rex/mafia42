package com.mafia.game.server.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserJoin {
    private String uuid;
    private String roomId;
    private String username;
}
