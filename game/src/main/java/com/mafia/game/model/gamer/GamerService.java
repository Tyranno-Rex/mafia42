package com.mafia.game.model.gamer;


import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GamerService {
    private final GamerRepository gamerRepository;


    public Gamer findByUserName(String UserName) {
        return gamerRepository.findByUserName(UserName);
    }

    public void save(Gamer gamer) {
        gamerRepository.save(gamer);
    }
}
