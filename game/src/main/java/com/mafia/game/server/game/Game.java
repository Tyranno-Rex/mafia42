package com.mafia.game.server.game;


import com.mafia.game.model.gamer.Gamer;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private int roomPlayerCount;
    private int roomMaxPlayerCount;
    private String Datetime;

    private int playerCount;
    private int mafiaCount;
    private int policeCount;
    private int doctorCount;
    private int citizenCount;

    @OneToMany
    private List<Gamer> players = new ArrayList<>();

    public Game() {
    }

    public Game(String roomName, String roomPassword,
                String roomStatus, String roomOwner,
                int roomPlayerCount, int roomMaxPlayerCount) {
        this.roomName = roomName;
        this.roomPassword = roomPassword;
        this.roomStatus = roomStatus;
        this.roomOwner = roomOwner;
        this.roomPlayerCount = roomPlayerCount;
        this.roomMaxPlayerCount = roomMaxPlayerCount;
        this.Datetime = LocalDateTime.now().toString();
    }

    public void addPlayer(Gamer gamer) {
        players.add(gamer);
    }
}
