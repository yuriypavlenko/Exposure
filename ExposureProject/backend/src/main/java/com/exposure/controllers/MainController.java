package com.exposure.controllers;

import com.exposure.DTOs.game.BotDTO;
import com.exposure.models.Bot;
import com.exposure.models.GameSession;
import com.exposure.models.User;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.GameSessionRepository;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/main")
public class MainController {
    private final BotRepository botRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;

    @Transactional
    @GetMapping
    public ResponseEntity<?> getPage(@RequestHeader("Authorization") String token) {
        Long userId = Long.parseLong(token);

        if (userRepository.findById(userId).isPresent()) {
            List<GameSession> activeSessions = gameSessionRepository.findAllByUserIdAndIsActiveTrue(userId);
            activeSessions.forEach(session -> session.setIsActive(false));

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    // Mock function
    @GetMapping("/bots")
    public List<BotDTO> getBots() {
        /*
        
        На будущее - нужно переделать фронт, чтобы был список ботов (просто нужно будет попробовать сделать
        Lazy loading)
        Ну и естественно, брать ботов из базы данных, а не хардкодить.

        */

        // Это решение временное, так что лайно. Оно ограничивает главную страничку до 2 ботов.
        Optional<Bot> bot1 = botRepository.findBotById(Long.parseLong("2"));
        Optional<Bot> bot2 = botRepository.findBotById(Long.parseLong("3"));

        if (bot1.isPresent() && bot2.isPresent()) {
            BotDTO botDTO1 = new BotDTO(bot1.get().getId(), bot1.get().getName());
            BotDTO botDTO2 = new BotDTO(bot2.get().getId(), bot2.get().getName());

            return List.of(botDTO1, botDTO2);
        }

        return null;
    }
}
