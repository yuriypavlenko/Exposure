package com.exposure.interfaces;

import com.exposure.models.Bot;
import com.exposure.models.Chat;
import com.exposure.models.GameSession;
import com.exposure.models.Story;

public interface BotResponseInterface {
    public String getResponse(Bot bot, String userMessage, Chat chat, Story story, GameSession gameSession);
}
