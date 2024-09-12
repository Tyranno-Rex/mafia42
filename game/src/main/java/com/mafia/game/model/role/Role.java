package com.mafia.game.model.role;

public abstract class Role {
    protected String name;

    public Role(String name) {
        this.name = name;
    }

    public abstract void uniqueAbility1();
    public abstract void uniqueAbility2();

    public String getName() {
        return name;
    }
}
