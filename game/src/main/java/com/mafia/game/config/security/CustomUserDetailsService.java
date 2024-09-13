package com.mafia.game.config.security;


import com.mafia.game.model.gamer.Gamer;
import com.mafia.game.model.gamer.GamerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final GamerRepository gamerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Gamer userData = gamerRepository.findByUserName(username);
        if (userData != null) {
            return new CustomUserDetails(userData);
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}

