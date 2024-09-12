package com.mafia.game.server.game;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;
    private String roomPassword;
    private String roomStatus;
    private String roomOwner;
    private String roomPlayerCount;
    private String roomMaxPlayerCount;
    private String Datetime;

    private int playerCount;
    private int mafiaCount;
    private int policeCount;
    private int doctorCount;
    private int citizenCount;

    public Game(String roomName, String roomPassword,
                String roomStatus, String roomOwner,
                String roomPlayerCount, String roomMaxPlayerCount) {
        this.roomName = roomName;
        this.roomPassword = roomPassword;
        this.roomStatus = roomStatus;
        this.roomOwner = roomOwner;
        this.roomPlayerCount = roomPlayerCount;
        this.roomMaxPlayerCount = roomMaxPlayerCount;
        this.Datetime = LocalDateTime.now().toString();
    }
}
