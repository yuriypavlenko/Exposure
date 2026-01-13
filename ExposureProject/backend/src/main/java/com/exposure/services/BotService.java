package com.exposure.services;

import com.exposure.DTOs.service.BotStates;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.Bot;
import com.exposure.models.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BotService implements BotResponseInterface {

    private final PromptGenerator promptGenerator;
    private final OllamaClient ollamaClient;

    public String DEFAULT_OLLAMA_MODEL = "llama3.1"; // TODO: перенести в файл конфигурации

    @Override
    public String getResponse(Bot bot, String question, BotStates botState, Chat chat) {
        String prompt = promptGenerator.generatePrompt(bot, question, botState, chat.getMessages());

        return ollamaClient.generate(DEFAULT_OLLAMA_MODEL, prompt);
    }
}
