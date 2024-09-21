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
    private String mafiaUser;
    private int policeCount;
    private String policeUser;
    private int doctorCount;
    private String doctorUser;
    private int citizenCount;
    private String citizenUser;

    @ManyToMany
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
        if (this.playerCount == 0) {
            this.roomStatus = "WAITING";
            this.playerCount++;
            this.roomPlayerCount++;
            this.players.add(gamer);
        } else{
            this.playerCount++;
            this.roomPlayerCount++;
            this.players.add(gamer);
        }
        System.out.println("Player added to the game: " + gamer.getUserName() + " size: " + players.size());
    }

    public void setGameStatus(String status) {
        this.roomStatus = status;
    }

    public void removePlayer(String userName) {
        for (Gamer gamer : players) {
            if (gamer.getUserName().equals(userName)) {
                this.playerCount--;
                this.roomPlayerCount--;
                players.remove(gamer);
                System.out.println("Player removed from the game: " + gamer.getUserName());
                return;
            }
        }
    }

    public void setPlayerRoles(int mafiaCount, String mafiaUser, int policeCount, String policeUser, int doctorCount, String doctorUser, int citizenCount, String citizenUser) {
        this.mafiaCount = mafiaCount;
        this.mafiaUser = mafiaUser;
        this.policeCount = policeCount;
        this.policeUser = policeUser;
        this.doctorCount = doctorCount;
        this.doctorUser = doctorUser;
        this.citizenCount = citizenCount;
        this.citizenUser = citizenUser;
    }
}
