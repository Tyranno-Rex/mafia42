package com.mafia.game.server.socket;

import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.server.game.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private GameService gameService;

    @MessageMapping("/message")
    public void messageSocket(Message message) throws Exception {
        messagingTemplate.convertAndSend("/topic/message/" + message.getGameId(), message);
    }

    @MessageMapping("/user")
    public void userGameSocket(Long roomId, List<Gamer> users) throws Exception {
        messagingTemplate.convertAndSend("/topic/user/" + roomId, users);
    }
}