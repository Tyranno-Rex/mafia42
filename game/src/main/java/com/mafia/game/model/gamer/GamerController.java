package com.mafia.game.model.gamer;


import com.mafia.game.model.gamer.gamerDTO.gamerSignUpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gamer")
public class GamerController {
    private final GamerService gamerService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/isExist")
    public Map<String, String> isExist(String userName) {
        Map<String, String> response = new HashMap<>();
        if (gamerService.findByUserName(userName) != null) {
            response.put("status", "true");
            return response;
        } else {
            response.put("status", "false");
            return response;
        }
    }

    @PostMapping("/signup")
    public Map<String, String> signUp(@RequestBody gamerSignUpDTO gamerSignUpDTO) {
        Map<String, String> response = new HashMap<>();
        try{
            Gamer gamer = new Gamer();
            gamer.setUserName(gamerSignUpDTO.getUserName());
            gamer.setUserPassword(passwordEncoder.encode(gamerSignUpDTO.getUserPassword()));
            gamer.setRole("ROLE_USER");
            gamerService.save(gamer);
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            response.put("status", "fail");
            return response;
        }
    }
}
