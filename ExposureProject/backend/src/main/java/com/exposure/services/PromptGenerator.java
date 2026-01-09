package com.exposure.services;

import com.exposure.DTOs.service.BotStates;
import com.exposure.models.Bot;
import org.springframework.stereotype.Service;


@Service
public class PromptGenerator {
    public String generatePrompt(Bot bot, String userMessage, BotStates botState) {
        String botName = bot.getName();
        String botPersonality = bot.getPersonality();

        StringBuilder prompt = new StringBuilder();

        prompt.append("Ты находишься в режиме ролевой игры. Твоя роль: ").append(botName).append(".\n");
        prompt.append("Твоя личность и предыстория: ").append(botPersonality).append(".\n\n");

        prompt.append("Инструкции по поведению:\n");
        prompt.append("- Отвечай строго в соответствии со своим характером.\n");
        prompt.append("- Не выходи из роли и не упоминай, что ты ИИ.\n");

        if (botState == BotStates.LYING) {
            prompt.append("- ВАЖНО: В данный момент ты должен ЛГАТЬ. Не говори правду пользователю.\n");
            prompt.append("- Твоя ложь должна быть убедительной и соответствовать твоему характеру.\n");
            prompt.append("- Старайся запутать пользователя, но не признавайся, что ты врешь.\n\n");
        } else {
            prompt.append("- Сейчас ты должен говорить ПРАВДУ.\n");
            prompt.append("- Будь честен в рамках своей роли.\n\n");
        }

        prompt.append("Текущее сообщение от пользователя: ").append(userMessage).append("\n");
        prompt.append("Твой ответ (от имени ").append(botName).append("):");

        return prompt.toString();
    }
}
