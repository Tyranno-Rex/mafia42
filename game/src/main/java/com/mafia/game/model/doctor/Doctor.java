package com.mafia.game.model.doctor;

import com.mafia.game.model.role.Role;
import lombok.Getter;


public class Doctor extends Role {

    private String user_id;
    private Long room_id;

    @Getter
    private boolean isHealed = false;
    @Getter
    private Long healedPlayerId = null;

    public Doctor(String user_id, Long room_id) {
        super("의사");
        this.user_id = user_id;
        this.room_id = room_id;
    }

    // Treatment : At night, heals one player from execution.
    @Override
    public void uniqueAbility1() {
        if (isHealed) {
            System.out.println(name + " has already healed a player.");
        } else {
            if (healedPlayerId == null) {
                System.out.println(name + " has not healed a player.");
            } else {
                System.out.println(name + " is healing a player.");
            }
            isHealed = true;
        }
    }

    public void setHealedPlayerId(Long healedPlayerId) {
        this.healedPlayerId = healedPlayerId;
    }

    public void resetHealed() {
        isHealed = false;
        healedPlayerId = null;
    }

    @Override
    public void uniqueAbility2() {
        System.out.println(name + " is healing a player.");
    }

}
