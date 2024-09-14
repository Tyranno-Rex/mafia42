package com.mafia.game.server.socket;

import com.mafia.game.model.gamer.GamerController;
import com.mafia.game.server.game.Game;
import com.mafia.game.server.game.GameController;
import com.mafia.game.server.game.GameService;

import com.mafia.game.server.game.GamerActivityEvent;
import com.mafia.game.server.socket.socketDto.CheckMessage;
import com.mafia.game.server.socket.socketDto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ApplicationEventPublisher eventPublisher;

    @MessageMapping("/message")
    public void messageSocket(Message message) throws Exception {
        messagingTemplate.convertAndSend("/topic/message/" + message.getGameId(), message);
    }

    @MessageMapping("/check")
    public void checkSocket(CheckMessage checkMessage) {
        eventPublisher.publishEvent(new GamerActivityEvent(checkMessage.getUsername()));
    }

    @MessageMapping("/game")
    public void GameSocket(Long roomId, Game game) throws Exception {
        messagingTemplate.convertAndSend("/topic/game/" + roomId, game);
    }
}