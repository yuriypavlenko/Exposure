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

            Optional<Bot> bot1 = botRepository.findBotById(selectedBotId.get(0));
            Optional<Bot> bot2 = botRepository.findBotById(selectedBotId.get(1));

            if (bot1.isPresent() && bot2.isPresent()) {
                List<Bot> bots = List.of(bot1.get(), bot2.get());

                int initialLimit = 5;
                GameSession gameSession = new GameSession(user.get(), bots, initialLimit);
                gameSessionRepository.save(gameSession);

                GameResponse gameResponse = new GameResponse(
                        gameSession.getId(),
                        List.of(new BotDTO(bot1.get().getId(), bot1.get().getName()),
                                new BotDTO(bot2.get().getId(), bot2.get().getName())),
                        gameSession.getQuestionsLeft()
                        );

                return ResponseEntity.ok(gameResponse);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @Transactional
    @PostMapping("/question")
    public ResponseEntity<?> onQuestion(@RequestBody QuestionRequest request) {
        Optional<User> user = userRepository.findById(request.userId);
        Optional<Bot> bot = botRepository.findBotById(request.botId);
        Optional<GameSession> gameSession = gameSessionRepository.findById(request.sessionId);
        String question = request.question;

        if (user.isPresent() && bot.isPresent() && gameSession.isPresent() && !question.isBlank()) {
            if (Objects.equals(gameSession.get().getUser().getId(), user.get().getId()) && gameSession.get().getBots().contains(bot.get())) {
                if (gameSession.get().getQuestionsLeft() > 0) {
                    String response = botResponseService.getResponse(bot.get(), question);

                    int questionsLeft = gameSession.get().decreaseQuestionLeft();

                    QuestionResponse questionResponse = new QuestionResponse(response, questionsLeft);

                    gameSessionRepository.save(gameSession.get());
                    return ResponseEntity.ok(questionResponse);
                } else {
                    return ResponseEntity.status(403).build(); // Вопросов больше не осталось
                }
            } else {
                return ResponseEntity.badRequest().build(); // Пользователь или бот не принадлежит сессии.
            }
        } else {
            return ResponseEntity.badRequest().build(); // какой-то из параметров отсутствует.
        }
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
