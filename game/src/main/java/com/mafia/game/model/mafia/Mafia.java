package com.mafia.game.model.mafia;

import com.mafia.game.model.role.Role;

public class Mafia extends Role {
    private String user_id;
    private Long room_id;

    private boolean isKilled = false;
    private Long killedPlayerId = null;

    public Mafia(String user_id, Long room_id) {
        super("마피아");
        this.user_id = user_id;
        this.room_id = room_id;
    }

    // Kill : At night, kills one player.
    @Override
    public void uniqueAbility1() {
        if (isKilled) {
            System.out.println(name + " has already killed a player.");
        } else {
            if (killedPlayerId == null) {
                System.out.println(name + " has not killed a player.");
            } else {
                System.out.println(name + " is killing a player.");
            }
            isKilled = true;
        }
    }

    public void setKilledPlayerId(Long killedPlayerId) {
        this.killedPlayerId = killedPlayerId;
    }

    public void resetKilled() {
        isKilled = false;
        killedPlayerId = null;
    }

    @Override
    public void uniqueAbility2() {
        System.out.println(name + " is killing a player.");
    }
}
