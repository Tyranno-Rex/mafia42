package com.mafia.game.server.socket;

import com.mafia.game.server.game.gameDto.GameSocketDTO;
import com.mafia.game.server.game.gameEvent.GamerActionEvent;
import com.mafia.game.server.game.gameEvent.GamerActivityEvent;
import com.mafia.game.server.game.gameStatus.GamePlayer;
import com.mafia.game.server.game.gameStatus.GameState;
import com.mafia.game.server.game.gameStatus.GameUser;
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
        if (message.getContent().startsWith("/mafia")) {
            String action = "mafia";
            String target = message.getContent().split(" ")[1];
            eventPublisher.publishEvent(new GamerActionEvent(Long.parseLong(message.getGameId()), message.getUuid(), action, target));
        }
        else if (message.getContent().startsWith("/police")) {
            String action = "police";
            String target = message.getContent().split(" ")[1];
            eventPublisher.publishEvent(new GamerActionEvent(Long.parseLong(message.getGameId()), message.getUuid(), action, target));
        }
        else if (message.getContent().startsWith("/doctor")) {
            String action = "doctor";
            String target = message.getContent().split(" ")[1];
            eventPublisher.publishEvent(new GamerActionEvent(Long.parseLong(message.getGameId()), message.getUuid(), action, target));
        }
        else if (message.getContent().startsWith("/vote1")) {
            String action = "vote1";
            String target = message.getContent().split(" ")[1];
            eventPublisher.publishEvent(new GamerActionEvent(Long.parseLong(message.getGameId()), message.getUuid(), action, target));
        }
        else if (message.getContent().startsWith("/vote2")){
            String action = "vote2";
            String target = message.getContent().split(" ")[1];
            eventPublisher.publishEvent(new GamerActionEvent(Long.parseLong(message.getGameId()), message.getUuid(), action, target));
        } else{
            messagingTemplate.convertAndSend("/topic/message/" + message.getGameId(), message);
        }
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

    @MessageMapping("/user")
    public void UserSocket(Long roomId, String userName, GameState game, String msg) throws Exception {
        String role = game.getGamePlayers().stream()
                .filter(player -> player.getUsername().equals(userName))
                .map(GamePlayer::getRole)
                .findFirst()
                .orElse(null);
        boolean alive = game.getGamePlayers().stream()
                .filter(player -> player.getUsername().equals(userName))
                .map(GamePlayer::getIsAlive)
                .findFirst()
                .orElse(false);
        GameUser gameUser = new GameUser(userName, role, msg, alive);
        messagingTemplate.convertAndSend("/topic/user/" + roomId + "/" + userName, gameUser);
    }
}
