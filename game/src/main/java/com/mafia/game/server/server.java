package com.mafia.game.server;

import com.mafia.game.server.game.Game;
import com.mafia.game.server.game.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class server {
    private Map<Long, Game> games = new ConcurrentHashMap<>();
    private final GameService gameService;
}
