package com.exposure.controllers;

import com.exposure.DTOs.Auth.AuthRequest;
import com.exposure.DTOs.game.*;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.Bot;
import com.exposure.models.GameSession;
import com.exposure.models.User;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.GameSessionRepository;
import com.exposure.repositories.UserRepository;
import com.exposure.services.BotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


/*

@TODO: Решить проблему с фронтом:
    Uncaught TypeError: can't access property "toUpperCase", bot.name is null

*/


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/game")
public class GameController {
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;

    private final BotResponseInterface botResponseService;


    @PostMapping("/start")
    public ResponseEntity<?> getPage(@RequestBody GameRequest request) {
        Optional<User> user = userRepository.findById(Long.parseLong(request.userId));
        List<Long> selectedBotId = request.selectedBotId;

        if (user.isPresent() && !selectedBotId.isEmpty()) {
            // Assuming exactly two bots are selected for the game session
            // Здесь код херня, нужно будет переделать когда будет возможность выбирать больше ботов.
            Optional<Bot> bot1 = botRepository.findBotById(selectedBotId.get(0));
            Optional<Bot> bot2 = botRepository.findBotById(selectedBotId.get(1));

            if (bot1.isPresent() && bot2.isPresent()) { // Хуяк-хуяк-хуяк и в продакшн.
                List<Bot> bots = List.of(bot1.get(), bot2.get());

                int initialLimit = 5; // Начальное количество вопросов на сессию. Костыль.
                GameSession gameSession = new GameSession(user.get(), bots, initialLimit);
                gameSessionRepository.save(gameSession);
                
                // Костыль, нужно будет переделать когда будет возможность выбирать больше ботов.
                GameResponse gameResponse = new GameResponse(
                        gameSession.getId(),
                        user.get().getId(),
                        List.of(new BotDTO(bot1.get().getId(), bot1.get().getName()),
                                new BotDTO(bot2.get().getId(), bot2.get().getName())),
                        gameSession.getQuestionsLeft()
                        );

                return ResponseEntity.ok(gameResponse);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /*
    java.lang.IllegalArgumentException: The given id must not be null - после ввода вопроса
    Внимание: вопросы закончились! Теперь вы должны выбрать, кому доверяете. - после нажатия на ок от первого alert.

    Еще почему-то выводится 403... И так, давай соберем все в кучу:
    Мы нажимаем играть, создается сессия и все такое.
    На фронте отображается 5 вопросов доступно.
    Когда мы пишем вопрос и нажимаем отправить происходит магия:
    не удалось получить доступ к серверу, потом ошибка, что вопросы закончились
    в базе данных вопросов все так же остается 5.
    Вопрос: че за херня?
    Скорее всего ошибка именно в трансфере или типе передачи данных. Вторая возможная причина - ошибка на фронте.

    Game Error:
        Object { message: "Request failed with status code 403", name: "AxiosError", code: "ERR_BAD_REQUEST",
        config: {…}, request: XMLHttpRequest, response: {…}, status: 403, stack: "", … }
     */

    @PostMapping("/question")
    public ResponseEntity<?> question(@RequestBody QuestionRequest request) {
        // ЛОГ ДЛЯ ОТЛАДКИ (посмотрите в консоль IDE)
        System.out.println("SessionID: " + request.sessionId + ", UserID: " + request.userId);

        if (request.userId == null || request.botId == null || request.sessionId == null) {
            return ResponseEntity.badRequest().body("ID cannot be null");
        }

        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Bot bot = botRepository.findById(request.botId) // Используйте стандартный findById
                .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
        GameSession gameSession = gameSessionRepository.findById(request.sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        // Линейные проверки (как вы и хотели)
        if (!gameSession.getUser().getId().equals(user.getId())) return ResponseEntity.badRequest().build();
        if (!gameSession.getBots().contains(bot)) return ResponseEntity.badRequest().build();
        if (gameSession.getQuestionsLeft() <= 0) return ResponseEntity.status(403).build();

        // Логика
        String response = botResponseService.getResponse(bot, request.question);
        gameSession.decreaseQuestionLeft(); // убедитесь, что внутри метод меняет состояние объекта
        gameSessionRepository.save(gameSession);

        return ResponseEntity.ok(new QuestionResponse(response, gameSession.getQuestionsLeft()));
    }


    @PostMapping("/trust")
    public void trust() {
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

    // TODO: Обработка завершения игровой сессии.
    @PostMapping("/endsession")
    public void endSession() {
        /*
        На вход должно приходить:
        - Айди пользователя
        - Айди сессии

        Здесь мы проверяем существует ли пользователь и сессия
        Проверяем есть ли в сессии пользователь
        - Если нет то отправляем код 400, Bad request.

        Если все ок, то завершаем сессию.
        */
    }
}
