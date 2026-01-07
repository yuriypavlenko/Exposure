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


/*

TODO: В игровой сессии и его DTO добавить количество вопросов.

*/


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

    
    @PostMapping("/question")
    public void onQuestion() {
        /*
        На вход должно приходить:
        - Айди пользователя
        - Айди бота которому задали вопрос
        - Айди сессии
        - Вопрос

        Здесь мы проверяем существует ли пользователь, бот и сессия
        Проверяем есть ли в сессии пользователь и бот
        Проверяем осталось ли количество вопросов или нет
        - Если нет то отправляем код 403.

        Если да, то мы отправляем бота и вопрос сервису
        Сервис ответит нам, после этого забираем в сессии
        один вопрос и после отправляем в DTO на фронт ответ.
        */
    }


    @PostMapping
    public void onChoice() {
        /*
        На вход должно приходить:
        - Айди пользователя
        - Айди бота которого выбрали
        - Айди сессии

        Здесь мы проверяем существует ли пользователь, бот и сессия
        Проверяем есть ли в сессии пользователь и бот
        - Если нет то отправляем код (хз, тут нужен какой то код который говорит что запрос неверный. 200? Bad request?)

        Если все ок, то проверяем является ли тот бот который выбран лжецом или говорил правду.
        P.s. нужно добавить в сессии еще получается ботов лжецов помимо массива с обычными ботами. Просто на будущее если будет несколько лжецов.
        */
    }

    // TODO: Обработка завершенния игровой сессии.
    @PostMapping("/endsession")
    public void endSession() {
        // тут игрок просто хочет закончить игровую сессию. Удаляешь сессию и отправляешь ответ пользователю.
    }
}
