package com.exposure.interfaces;

import com.exposure.DTOs.service.BotStates;
import com.exposure.models.Bot;

public interface BotResponseInterface {
    public String getResponse(Bot bot, String userMessage, BotStates state);
}
