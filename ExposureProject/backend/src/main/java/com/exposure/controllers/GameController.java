package com.exposure.controllers;

import com.exposure.DTOs.Auth.AuthRequest;
import com.exposure.DTOs.game.BotDTO;
import com.exposure.DTOs.game.GameRequest;
import com.exposure.DTOs.game.GameResponse;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.Bot;
import com.exposure.models.GameSession;
import com.exposure.models.User;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.GameSessionRepository;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/game")
public class GameController {
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;

    private final BotResponseInterface botResponseInterface;


    @PostMapping("/start")
    public ResponseEntity<?> getPage(@RequestBody GameRequest request) {
        Optional<User> user = userRepository.findById(Long.parseLong(request.userId));
        List<Long> selectedBotId = request.selectedBotId;

        if (user.isPresent() && !selectedBotId.isEmpty()) {

            Optional<Bot> bot1 = botRepository.findBotById(selectedBotId.get(0));
            Optional<Bot> bot2 = botRepository.findBotById(selectedBotId.get(1));

            if (bot1.isPresent() && bot2.isPresent()) {
                List<Bot> bots = List.of(bot1.get(), bot2.get());

                GameSession gameSession = new GameSession(user.get(), bots);
                gameSessionRepository.save(gameSession);

                GameResponse gameResponse = new GameResponse(
                        gameSession.getId(),
                        List.of(new BotDTO(bot1.get().getId(), bot1.get().getName()),
                                new BotDTO(bot2.get().getId(), bot2.get().getName()))
                        );

                return ResponseEntity.ok(gameResponse);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    // TODO: На вопрос он спрашивает у бота через
    //  клиент оллама и отправляет назад ответ
    @PostMapping("/question")
    public void onQuestion() {
        // берешь с фронта запрос, токен. По токену проверяешь игрока, активную сессию.
        // Если все ок, то стандартно просто берешь вопрос, из ззапроса айди бота, достаешь бота и кидаешь в аи и возвращаешь ответ.
    }

    // TODO: Когда игрок выбирает бота которому верит происходит проверка
    //  и вывод результата
    @PostMapping
    public void onChoice() {
        // опять таки, проверяешь есть ли игрок, все ли ок с сессией.
        // после этого проверяешь по сессии правильного бота ли выбрал игрок по айди или нет и отправляешь ответ.
    }

    // TODO: Обработка завершенния игровой сессии.
    @PostMapping("/endsession")
    public void endSession() {
        // тут игрок просто хочет закончить игровую сессию. Удаляешь сессию и отправляешь ответ пользователю.
    }
}
