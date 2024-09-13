package com.mafia.game.model.gamer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GamerRepository extends JpaRepository<Gamer, Long> {
    Gamer findByUserName(String userName);
}
