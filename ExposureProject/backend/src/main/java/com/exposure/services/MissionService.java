package com.exposure.services;

import com.exposure.DTOs.service.AI.RolesData;
import com.exposure.DTOs.service.AI.StoryResponse;
import com.exposure.events.GameSessionCancelledEvent;
import com.exposure.events.GameSessionCreatedEvent;
import com.exposure.models.*;
import com.exposure.repositories.GameSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;



@Service
@RequiredArgsConstructor
public class MissionService {
    private final GameSessionRepository gameSessionRepository;
    private final StoryGeneratorService storyGeneratorService;

    private final Logger logger = LoggerFactory.getLogger(MissionService.class);

    private final Map<Long, CompletableFuture<?>> runningTasks = new ConcurrentHashMap<>();
    private final ObjectProvider<MissionService> selfProvider;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameStarted(GameSessionCreatedEvent event) {
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            selfProvider.getIfAvailable().processStoryGeneration(event);
        });

        runningTasks.put(event.sessionId(), task);

        task.whenComplete((res, ex) -> runningTasks.remove(event.sessionId()));
    }

    @EventListener
    public void handleCancel(GameSessionCancelledEvent event) {
        CompletableFuture<?> task = runningTasks.remove(event.sessionId());
        if (task != null) {
            task.cancel(true);
            logger.info("Generation canceled for session {}.", event.sessionId());
        }
    }

    @Transactional
    public void processStoryGeneration(GameSessionCreatedEvent event) {
        if (Thread.currentThread().isInterrupted()) return;

        GameSession session = gameSessionRepository.findById(event.sessionId())
                .orElseThrow(() -> new RuntimeException("Session not found after commit!"));

        try {
            Map<String, Object> response = storyGeneratorService.generateStory(
                    session.getMission(), event.botsCount(), event.lyingBotsCount()
            );

            if (Thread.currentThread().isInterrupted()) return;

            Story story = (Story) response.get("story");
            StoryResponse storyResponse = (StoryResponse) response.get("storyResponse");

            List<SessionBotRole> botRoles = assignRoles(session, storyResponse, session.getBots());
            session.setBotRoles(botRoles);

            session.setStatus(GameStatus.READY);
            session.setStory(story);

            gameSessionRepository.save(session);

        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException e) {
            logger.info("Session {} was updated (probably closed). Generation canceled automatically.", event.sessionId());
        } catch (Exception e) {
            logger.error("Error generation story: ", e);
            session.setStatus(GameStatus.FAILED);
            gameSessionRepository.save(session);
        }
    }

    public List<SessionBotRole> assignRoles(GameSession session, StoryResponse storyData, List<Bot> bots) {
        List<SessionBotRole> assignments = new ArrayList<>();
        List<RolesData> rolesJson = storyData.roles_data();

        RolesData guiltyRole = rolesJson.stream()
                .filter(RolesData::isGuilty)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No guilty role in JSON"));

        Bot guiltyBot = session.getLyingBots().get(0);

        assignments.add(new SessionBotRole(null, session, guiltyBot, guiltyRole.role(), true));

        List<Bot> otherBots = bots.stream().filter(b -> !b.getId().equals(guiltyBot.getId())).toList();
        List<RolesData> otherRoles = rolesJson.stream().filter(r -> !r.isGuilty()).toList();

        for (int i = 0; i < otherBots.size(); i++) {
            assignments.add(new SessionBotRole(null, session, otherBots.get(i), otherRoles.get(i).role(), false));
        }

        return assignments;
    }
}
