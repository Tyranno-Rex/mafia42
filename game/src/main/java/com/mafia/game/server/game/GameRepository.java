package com.mafia.game.server.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("SELECT g FROM Game g WHERE g.roomStatus != 'SHUTDOWN'")
    List<Game> findByGameStatusNot();
}
