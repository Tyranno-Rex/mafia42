package com.mafia.game.server.socket.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;



//@Component
//public class RedisMessageSubscriber implements MessageListener {
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        String channel = new String(message.getChannel());
//        String messageBody = new String(message.getBody());
//
//        // Redis 채널을 WebSocket 토픽으로 변환
//        String destination = channel.replace("topic", "/topic");
//
//        messagingTemplate.convertAndSend(destination, messageBody);
//    }
//}
