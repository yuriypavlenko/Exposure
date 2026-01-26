package com.exposure.services;

import com.exposure.DTOs.service.AI.RolesData;
import com.exposure.DTOs.service.AI.StoryResponse;
import com.exposure.DTOs.service.AI.TimelineEvent;
import com.exposure.DTOs.service.BotService.BotPromptContext;
import com.exposure.DTOs.service.BotStates;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.*;
import com.exposure.repositories.SessionBotRoleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BotService implements BotResponseInterface {
    private final MessagePromptGenerator messagePromptGenerator;
    private final ChatClient chatClient;
    private final SessionBotRoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public String getResponse(Bot bot, String user_message, Chat chat, Story story, GameSession session) {
        SessionBotRole botRole = roleRepository.findBySessionAndBot(session, bot)
                .orElseThrow(() -> new RuntimeException("Bot role not found"));

        StoryResponse storyData;
        try {
            storyData = objectMapper.readValue(story.getGeneratedStoryJson(), StoryResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse story JSON", e);
        }

        RolesData roleJsonData = storyData.roles_data().stream()
                .filter(r -> r.role().equals(botRole.getRoleIdentifier()))
                .findFirst()
                .orElseThrow();

        List<TimelineEvent> myEvents = storyData.truth_timeline().stream()
                .filter(e -> e.witnesses().contains(botRole.getRoleIdentifier()))
                .toList();

        BotPromptContext context = new BotPromptContext(
                bot.getName(),
                bot.getPersonality(),
                storyData.story_meta().description(),
                roleJsonData.role_description(),
                session.isBotLying(bot.getId()) ? roleJsonData.alibi() : roleJsonData.actual(),
                roleJsonData.motive(),
                myEvents
        );

        String prompt = messagePromptGenerator.generatePrompt(context, user_message, chat.getMessages());

        return chatClient.prompt(prompt).call().content();
    }
}
