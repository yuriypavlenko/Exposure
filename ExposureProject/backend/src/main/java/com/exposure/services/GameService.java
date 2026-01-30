package com.exposure.services;

import com.exposure.events.GameSessionCreatedEvent;
import com.exposure.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {
    public GameSession createGameSession(User user, List<Bot> bots, Mission mission) {
        int initialLimit = mission.getInitialQuestionsAmount();
        List<Bot> lyingBots = getRandomLyingBots(bots);

        GameSession gameSession = new GameSession(user, bots, lyingBots, initialLimit, mission);

        // Creating chats and adding bots.
        for (Bot bot : bots) {
            Chat chat = new Chat();

            chat.getMembers().add(user);
            chat.getMembers().add(bot);

            gameSession.addChat(chat);
        }

        return gameSession;
    }


    // Logic of getting random lying bot.
    private List<Bot> getRandomLyingBots(List<Bot> bots) {
        List<Bot> mutableBots = new ArrayList<>(bots);
        Collections.shuffle(mutableBots);
        Bot randomLiar = mutableBots.getFirst();

        return List.of(randomLiar);
    }
}
