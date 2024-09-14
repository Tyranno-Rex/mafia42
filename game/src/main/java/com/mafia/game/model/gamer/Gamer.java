package com.mafia.game.model.gamer;

import com.mafia.game.server.game.Game;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Gamer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private String userPassword;
    private String role;

    @ManyToMany(mappedBy = "players")
    private List<Game> games = new ArrayList<>();
}
