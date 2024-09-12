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


    // 0.5초당 한번씩 실행
    @Scheduled(fixedRate = 500)
    public void gameLoop() {
        for (Game game : games.values()) {
//            game.update();
        }
    }
}
