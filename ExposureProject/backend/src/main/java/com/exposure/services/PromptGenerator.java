package com.exposure.services;

import com.exposure.DTOs.service.BotStates;
import com.exposure.models.Bot;
import com.exposure.models.Message;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PromptGenerator {
    public String generatePrompt(Bot bot, String userMessage, BotStates botState, List<Message> history) {
        StringBuilder prompt = new StringBuilder();

        // 1. Системная установка (Личность)
        prompt.append("Ты — ").append(bot.getName()).append(". ")
                .append(bot.getPersonality()).append("\n");

        // 2. Инструкция по состоянию (Ложь/Правда)
        if (botState == BotStates.LYING) {
            prompt.append("ВАЖНО: Сейчас ты должен ЛГАТЬ. Будь убедительным, но не говори правду.\n");
        } else {
            prompt.append("Сейчас ты должен говорить ПРАВДУ.\n");
        }

        prompt.append("\nИстория диалога:\n");

        // 3. Добавление истории (последние 10 сообщений для экономии памяти)
        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            Message msg = history.get(i);
            String role = msg.getSender().getId().equals(bot.getId()) ? bot.getName() : "Пользователь";
            prompt.append(role).append(": ").append(msg.getText()).append("\n");
        }

        // 4. Текущий вопрос
        prompt.append("Пользователь: ").append(userMessage).append("\n");
        prompt.append(bot.getName()).append(":");

        return prompt.toString();
    }
}

