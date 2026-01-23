package com.exposure.controllers;

import com.exposure.DTOs.game.*;
import com.exposure.DTOs.service.BotStates;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.*;
import com.exposure.repositories.*;
import com.exposure.services.MissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/game")
public class GameController {
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final MissionRepository missionRepository;
    private final StoryRepository storyRepository;

    private final MissionService missionService;
    private final BotResponseInterface botResponseService;

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Transactional
    @PostMapping("/start")
    public ResponseEntity<?> getPage(@RequestBody GameRequest request) { // TODO: Переделать GameRequest - добавить выбор Id миссии.
        Optional<User> userOpt = userRepository.findById(Long.parseLong(request.userId));
        List<Long> selectedBotIds = request.selectedBotId;

        // TODO: Добавить сюда выборку Id миссии из request.
        Optional<Mission> missionOpt = missionRepository.findById(Long.parseLong("1")); // Mock


        if (userOpt.isPresent()
                && selectedBotIds != null
                && !selectedBotIds.isEmpty()
                && missionOpt.isPresent()) {

            List<Bot> bots = botRepository.findAllById(selectedBotIds);

            if (bots.size() == selectedBotIds.size()) {
                User user = userOpt.get();

                List<Bot> mutableBots = new ArrayList<>(bots);
                Collections.shuffle(mutableBots);
                Bot randomLiar = mutableBots.getFirst();
                List<Bot> lyingBots = List.of(randomLiar);


                Mission mission = missionOpt.get();
                Story story;
                try {
                    story = missionService.generateStory(mission, bots.size(), lyingBots.size());
                } catch (Exception e) {
                    logger.error(e.toString());
                    return ResponseEntity.status(500).build();
                }


                int initialLimit = 5; // TODO: Убрать в будущем эту логику в настройки.

                GameSession gameSession = new GameSession(user, bots, lyingBots, initialLimit);

                for (Bot bot : bots) {
                    Chat chat = new Chat();

                    chat.getMembers().add(user);
                    chat.getMembers().add(bot);

                    gameSession.addChat(chat);
                }

                gameSessionRepository.save(gameSession); // Поскольку сделал метод транзакционным то вроде можно убирать.
                // P.s. Я не знаю, нужно ли делать @Transactional или нет. У меня тут просто Story генерируется... просто для безопасности.

                List<BotDTO> botDTOs = bots.stream()
                        .map(b -> new BotDTO(b.getId(), b.getName()))
                        .toList();

                return ResponseEntity.ok(new GameResponse(
                        gameSession.getId(),
                        user.getId(),
                        botDTOs,
                        gameSession.getQuestionsLeft()
                ));
            }
        }

        return ResponseEntity.badRequest().build();
    }


    @PostMapping("/question")
    @Transactional
    public ResponseEntity<?> question(@RequestBody QuestionRequest request) {
        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Bot bot = botRepository.findById(request.botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
        GameSession gameSession = gameSessionRepository.findById(request.sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!gameSession.getUser().getId().equals(user.getId())) return ResponseEntity.badRequest().build();
        if (!gameSession.getBots().contains(bot)) return ResponseEntity.badRequest().build();
        if (gameSession.getQuestionsLeft() <= 0) return ResponseEntity.status(403).body("No questions left");

        Chat chat = gameSession.getChats().stream()
                .filter(c -> c.getMembers().contains(user) && c.getMembers().contains(bot))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chat between user and bot not initialized"));

        BotStates state = gameSession.isBotLying(bot.getId()) ? BotStates.LYING : BotStates.NOT_LYING;
        String botResponseText = botResponseService.getResponse(bot, request.question, state, chat);

        if (botResponseText != null && botResponseText.length() > 1000) {
            botResponseText = botResponseText.substring(0, 997) + "...";
        }

        saveMessage(chat, user, request.question);
        saveMessage(chat, bot, botResponseText);

        gameSession.decreaseQuestionLeft();
        return ResponseEntity.ok(new QuestionResponse(botResponseText, gameSession.getQuestionsLeft()));
    }

    private void saveMessage(Chat chat, SessionMember sender, String text) {
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setText(text);
        message.setSentAt(LocalDateTime.now());

        chat.getMessages().add(message);
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
}
