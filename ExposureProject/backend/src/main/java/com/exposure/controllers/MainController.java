package com.exposure.controllers;

import com.exposure.DTOs.game.BotDTO;
import com.exposure.DTOs.main.MissionInfo;
import com.exposure.events.GameSessionCancelledEvent;
import com.exposure.models.Bot;
import com.exposure.models.GameSession;
import com.exposure.models.Mission;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.GameSessionRepository;
import com.exposure.repositories.MissionRepository;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/main")
public class MainController {
    private final BotRepository botRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;

    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final ApplicationEventPublisher eventPublisher;


    @GetMapping
    public ResponseEntity<?> getPage(@RequestHeader("Authorization") String tokenStr) {
        try {
            Long userId = Long.parseLong(tokenStr);
            userRepository.findById(userId).orElseThrow();

            List<GameSession> activeSessions = gameSessionRepository.findAllByUserIdAndIsActiveTrue(userId);

            for (GameSession session : activeSessions) {
                session.setIsActive(false);
                eventPublisher.publishEvent(new GameSessionCancelledEvent(session.getId()));
            }

            gameSessionRepository.saveAll(activeSessions);

            return ResponseEntity.ok().build();
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.info("Session was updated by another process, closing skipped or handled elsewhere.");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error while getting Main page: ", e);
            return ResponseEntity.badRequest().build();
        }
    }


    // TODO: После изменить на Lazy loading
    @GetMapping("/bots")
    public ResponseEntity<?> getBots() {
        try {
            List<Bot> bots = botRepository.findAll();

            List<BotDTO> botDTOs = bots.stream()
                    .map(b -> new BotDTO(b.getId(), b.getName()))
                    .toList();

            return ResponseEntity.ok(botDTOs);
        } catch (Exception e) {
            logger.error("Error while getting bots: ", e);
            return ResponseEntity.badRequest().build();
        }
    }


    // TODO: После изменить на Lazy loading
    @GetMapping("/missions")
    public ResponseEntity<?> getMissions() {
        try {
            List<Mission> missions = missionRepository.findAll();
            List<MissionInfo> missionDTOs = missions.stream()
                    .map(m -> new MissionInfo(m.getId(), m.getTitle()))
                    .toList();

            return ResponseEntity.ok(missionDTOs);
        } catch (Exception e) {
            logger.error("Error while getting missions: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
