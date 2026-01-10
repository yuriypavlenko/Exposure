package com.exposure.controllers;

import com.exposure.DTOs.game.BotDTO;
import com.exposure.models.Bot;
import com.exposure.repositories.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/main")
public class MainController {
    private final BotRepository botRepository;

    @GetMapping
    public String getPage() {

        // TODO: если игрок на главной страничке то удаляем его игровые сессии, он ведь уже не в игре
        // Не удаляем а деактивируем, чтобы потом можно было восстановить игру если нужно или просмотреть историю.
        // Для этого нужно будет добавить поле "активна" в сущность GameSession.

        return null;
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
