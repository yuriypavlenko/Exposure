package com.exposure.controllers;

import com.exposure.DTOs.game.*;
import com.exposure.DTOs.service.BotStates;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.Bot;
import com.exposure.models.GameSession;
import com.exposure.models.User;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.GameSessionRepository;
import com.exposure.repositories.UserRepository;
import jakarta.transaction.Transactional;
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

    private final BotResponseInterface botResponseService;


    @PostMapping("/start")
    public ResponseEntity<?> getPage(@RequestBody GameRequest request) {
        Optional<User> user = userRepository.findById(Long.parseLong(request.userId));
        List<Long> selectedBotIds = request.selectedBotId;

        if (user.isPresent() && selectedBotIds != null && !selectedBotIds.isEmpty()) {
            List<Bot> bots = botRepository.findAllById(selectedBotIds);

            if (bots.size() == selectedBotIds.size()) {
                java.util.Collections.shuffle(new java.util.ArrayList<>(bots));
                Bot randomLiar = bots.get(new java.util.Random().nextInt(bots.size()));
                List<Bot> lyingBots = List.of(randomLiar);
                
                // Думаю потом можно добавить это как настройку перед началом игры.
                int initialLimit = 5; // Потом переместить это, а не магически колдовать числа.


                // Тут еще очень важно добавить в сессию ссылки на чаты ботов и сохранить их в сессии.
                // (ну и естественно создать чаты и сообщения)
                GameSession gameSession = new GameSession(user.get(), bots, lyingBots, initialLimit);
                gameSessionRepository.save(gameSession);

                List<BotDTO> botDTOs = bots.stream()
                        .map(b -> new BotDTO(b.getId(), b.getName()))
                        .toList();

                GameResponse gameResponse = new GameResponse(
                        gameSession.getId(),
                        user.get().getId(),
                        botDTOs,
                        gameSession.getQuestionsLeft()
                );

                return ResponseEntity.ok(gameResponse);
            }
        }
        return ResponseEntity.badRequest().build();
    }


    // TODO: добавить чаты, сообщения и добавлять их по мере игры.
    @PostMapping("/question")
    @Transactional
    public ResponseEntity<?> question(@RequestBody QuestionRequest request) {
        if (request.userId == null || request.botId == null || request.sessionId == null) {
            return ResponseEntity.badRequest().body("ID cannot be null");
        }

        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Bot bot = botRepository.findById(request.botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
        GameSession gameSession = gameSessionRepository.findById(request.sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!gameSession.getUser().getId().equals(user.getId())) return ResponseEntity.badRequest().build();
        if (!gameSession.getBots().contains(bot)) return ResponseEntity.badRequest().build();
        if (gameSession.getQuestionsLeft() <= 0) return ResponseEntity.status(403).build();

        BotStates state = gameSession.isBotLying(bot.getId()) ? BotStates.LYING : BotStates.NOT_LYING;
        String response = botResponseService.getResponse(bot, request.question, state);
        
        // Здесь мы добавляем в чат сообщение от бота и игрока.

        gameSession.decreaseQuestionLeft();
        gameSessionRepository.save(gameSession);

        return ResponseEntity.ok(new QuestionResponse(response, gameSession.getQuestionsLeft()));
    }


    @PostMapping("/choice")
    public ResponseEntity<?> choice(@RequestBody ChoiceRequest request) {
        if (request.userId == null || request.botId == null || request.sessionId == null) {
            return ResponseEntity.badRequest().body("ID cannot be null");
        }

        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Bot bot = botRepository.findById(request.botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
        GameSession gameSession = gameSessionRepository.findById(request.sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!gameSession.getUser().getId().equals(user.getId())) return ResponseEntity.badRequest().build();
        if (!gameSession.getBots().contains(bot)) return ResponseEntity.badRequest().build();

        if (gameSession.isBotLying(bot.getId())) {
            return ResponseEntity.ok(new ChoiceResponse(true, bot.getId()));
        } else {
            return ResponseEntity.ok(new ChoiceResponse(false, bot.getId()));
        }
    }

    // Думаю можно удалить ведь у нас и так будет сессия чиститься по приходу в main.
    //@PostMapping("/endsession")
    //public void endSession() {
        /*
        На вход должно приходить:
        - Айди пользователя
        - Айди сессии

        Здесь мы проверяем существует ли пользователь и сессия
        Проверяем есть ли в сессии пользователь
        - Если нет то отправляем код 400, Bad request.

        Если все ок, то завершаем сессию.
        */
    //}
}
