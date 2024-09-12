package com.mafia.game.server.chatting;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message")
    public void greeting(Message message) throws Exception {
        System.out.println("MessageController: " + message.getContent() + " " + message.getUuid() + " " + message.getRoomId());
        messagingTemplate.convertAndSend("/topic/message/" + message.getRoomId(), message);
    }
}