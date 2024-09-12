package com.mafia.game.model.police;

import com.mafia.game.model.role.Role;

public class Police extends Role {
    private String user_id;
    private Long room_id;

    private boolean isInvestigated = false;
    private Long investigatedPlayerId = null;

    public Police(String user_id, Long room_id) {
        super("경찰");
        this.user_id = user_id;
        this.room_id = room_id;
    }

    @Override
    public void uniqueAbility1() {
        if (isInvestigated) {
            System.out.println(name + " has already investigated a player.");
        } else {
            if (investigatedPlayerId == null) {
                System.out.println(name + " has not investigated a player.");
            } else {
                System.out.println(name + " is investigating a player.");
            }
            isInvestigated = true;
        }
    }

    public void setInvestigatedPlayerId(Long investigatedPlayerId) {
        this.investigatedPlayerId = investigatedPlayerId;
    }

    public void resetInvestigated() {
        isInvestigated = false;
        investigatedPlayerId = null;
    }

    @Override
    public void uniqueAbility2() {
        System.out.println(name + " is investigating a player.");
    }
}
