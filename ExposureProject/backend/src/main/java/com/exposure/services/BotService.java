package com.exposure.services;

import com.exposure.DTOs.service.BotStates;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.Bot;
import com.exposure.models.Chat;
import com.exposure.models.Story;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BotService implements BotResponseInterface {
    private final MessagePromptGenerator messagePromptGenerator;
    private final ChatClient chatClient;

    @Override
    public String getResponse(Bot bot, String question, BotStates botState, Chat chat, Story story) {



        String prompt = messagePromptGenerator.generatePrompt(bot, question, botState, chat.getMessages());

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
