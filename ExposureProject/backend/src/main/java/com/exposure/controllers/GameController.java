package com.exposure.controllers;

import com.exposure.DTOs.game.*;
import com.exposure.DTOs.main.InitializeGame;
import com.exposure.events.GameSessionCreatedEvent;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.*;
import com.exposure.repositories.*;
import com.exposure.services.GameService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:5173")
public class GameController {
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final MissionRepository missionRepository;

    private final BotResponseInterface botResponseService;
    private final GameService gameService;

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final ApplicationEventPublisher eventPublisher;



    @Transactional
    @PostMapping("/start")
    public ResponseEntity<?> startGame(@RequestBody GameRequest request) {
        try {
            if (request.userId == null || request.selectedBotIds == null) {
                return ResponseEntity.badRequest().build();
            }

            User user = userRepository.findById(Long.parseLong(request.userId)).orElseThrow();

            // If user already have active sessions.
            if (!gameSessionRepository.findAllByUserIdAndIsActiveTrue(user.getId()).isEmpty()) {
                logger.error("User already have active session.");
                return ResponseEntity.badRequest().build();
            }

            List<Long> selectedBotIds = request.selectedBotIds;
            Long missionId = request.missionId != null ? request.missionId : 1L;
            Mission mission = missionRepository.findById(missionId).orElseThrow();
            int initialLimit = mission.getInitialQuestionsAmount();

            // If some parameters are null or not presented.
            if (selectedBotIds == null || selectedBotIds.isEmpty()) {
                logger.error("Parameters are null or empty.");
                return ResponseEntity.badRequest().build();
            }

            List<Bot> bots = botRepository.findAllById(selectedBotIds);

            // If size of bots not equals size of selected bot ids.
            if (bots.size() != selectedBotIds.size()) {
                logger.error("Size of bots are not equal size of selected bot ids");
                return ResponseEntity.badRequest().build();
            }

            GameSession gameSession = gameService.createGameSession(user, bots, mission);
            gameSessionRepository.save(gameSession);

            // Event for mission service to generate story.
            eventPublisher.publishEvent(new GameSessionCreatedEvent(
                    gameSession.getId(), gameSession.getBots().size(), gameSession.getLyingBots().size()
            ));

            // Creating DTO to return front response.
            List<BotDTO> botDTOs = bots.stream()
                    .map(b -> new BotDTO(b.getId(), b.getName()))
                    .toList();

            return ResponseEntity.ok(new InitializeGame(gameSession.getId(), botDTOs, initialLimit));

        } catch (Exception e) {
            logger.error("Error while starting game: ", e);
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/question")
    @Transactional
    public ResponseEntity<?> question(@RequestBody QuestionRequest request) {
        try {
            User user = userRepository.findById(request.userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Bot bot = botRepository.findById(request.botId)
                    .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
            GameSession gameSession = gameSessionRepository.findById(request.sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found"));

            // Session doesn't contains user
            if (!gameSession.getUser().getId().equals(user.getId())) {
                logger.error("Session doesn't contains user.");
                return ResponseEntity.badRequest().build();
            }

            // Session doesn't contains bot
            if (!gameSession.getBots().contains(bot)) {
                logger.error("Session doesn't contains bot.");
                return ResponseEntity.badRequest().build();
            }

            // No questions left
            if (gameSession.getQuestionsLeft() <= 0) {
                logger.warn("No questions left.");
                return ResponseEntity.status(403).body("No questions left");
            }

            // Chat between bot and user does not exist.
            Chat chat = gameSession.getChats().stream()
                    .filter(c -> c.getMembers().contains(user) && c.getMembers().contains(bot))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Chat between user and bot not initialized"));


            Story story = gameSession.getStory();

            String botResponseText = botResponseService.getResponse(bot, request.question, chat, story, gameSession);

            // If length of LLM response too big.
            if (botResponseText != null && botResponseText.length() > 1000) {
                botResponseText = botResponseText.substring(0, 997) + "...";
            }

            chat.addMessage(user, request.question);
            chat.addMessage(bot, botResponseText);

            gameSession.decreaseQuestionLeft();

            return ResponseEntity.ok(new QuestionResponse(botResponseText, gameSession.getQuestionsLeft()));
        } catch (Exception e) {
            logger.error("Error while processing question: ", e);
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/choice")
    public ResponseEntity<?> choice(@RequestBody ChoiceRequest request) {
        try {
            if (request.userId == null || request.botId == null || request.sessionId == null) {
                logger.error("Error while processing choice: ID cannot be null.");
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/mission/{sessionId}")
    public ResponseEntity<?> missions(@PathVariable("sessionId") String sessionIdStr) {
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            GameSession gameSession = gameSessionRepository.findById(sessionId).orElseThrow();
            Mission mission = gameSession.getMission();

            if (mission != null) {
                GameMissionResponse response = new GameMissionResponse(
                        mission.getTitle(),
                        mission.getDescription(),
                        mission.getInitialQuestionsAmount());

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error in \"/mission\": ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/status/{sessionId}")
    public ResponseEntity<?> status(@PathVariable("sessionId") String sessionId) {
        try {
            Long id = Long.parseLong(sessionId);
            GameSession gameSession = gameSessionRepository.findById(id).orElseThrow();

            Map<String, String> response = new HashMap<>();

            if (gameSession.getStatus().equals(GameStatus.READY) && gameSession.getStory() != null) {
                response.put("status", "READY");
                return ResponseEntity.ok(response);
            }

            response.put("status", "GENERATING");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Status controller error: ", e);
            return ResponseEntity.status(404).body(Map.of("error", "Session not found"));
        }
    }
}
