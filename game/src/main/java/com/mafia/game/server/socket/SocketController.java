package com.mafia.game.server.socket;

import com.mafia.game.server.game.*;
import com.mafia.game.server.game.gameDto.GameSocketDTO;
import com.mafia.game.server.game.gameStatus.GameState;
import com.mafia.game.server.socket.socketDto.CheckMessage;
import com.mafia.game.server.socket.socketDto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    public void GameSocket(Long roomId, GameState game) throws Exception {
        GameSocketDTO gameSocketDTO = game.toDTO();
        messagingTemplate.convertAndSend("/topic/game/" + roomId, gameSocketDTO);
    }
}

//@Controller
//@RequiredArgsConstructor
//public class SocketController {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    private final ApplicationEventPublisher eventPublisher;
//
//    @MessageMapping("/message")
//    public void messageSocket(Message message) {
//        redisTemplate.convertAndSend("topic/message/" + message.getGameId(), message);
//    }
//
//    @MessageMapping("/check")
//    public void checkSocket(CheckMessage checkMessage) {
//        eventPublisher.publishEvent(new GamerActivityEvent(checkMessage.getUsername()));
//    }
//
//    @MessageMapping("/game")
//    public void GameSocket(Long roomId, GameState game) {
//        GameSocketDTO gameSocketDTO = game.toDTO();
//        redisTemplate.convertAndSend("topic/game/" + roomId, gameSocketDTO);
//    }
//}
//