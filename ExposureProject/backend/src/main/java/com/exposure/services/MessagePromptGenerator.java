package com.exposure.services;

import com.exposure.DTOs.service.AI.TimelineEvent;
import com.exposure.DTOs.service.BotService.BotPromptContext;
import com.exposure.DTOs.service.BotStates;
import com.exposure.models.Bot;
import com.exposure.models.Message;
import com.exposure.models.Story;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class MessagePromptGenerator {

    public String generatePrompt(BotPromptContext ctx, String userMessage, List<Message> history) {
        return """
            SYSTEM ROLE:
            You are %s. Your personality: %s.
            
            BACKGROUND (General knowledge):
            %s
            
            YOUR IDENTITY AND LEGEND:
            Your role: %s.
            Your version of events (STRICTLY ADHERE TO THIS): %s.
            %s
            
            YOUR MEMORIES (Events you witnessed):
            %s
            
            INSTRUCTIONS:
            - Respond as a human being in a chat, staying in character.
            - Do not contradict your version of events.
            - If asked about something you didn't witness, improvise but stay consistent with your legend.
            - Keep your responses concise (1-3 sentences).
            
            CONVERSATION HISTORY:
            %s
            Detective: %s
            %s:""".formatted(
                ctx.botName(), ctx.personality(),
                ctx.missionDescription(),
                ctx.roleDescription(),
                ctx.factToStickTo(),
                ctx.motive() != null ? "Hidden motive (do not reveal easily): " + ctx.motive() : "",
                formatEvents(ctx.relevantEvents()),
                formatHistory(history, ctx.botName()),
                userMessage,
                ctx.botName()
        );
    }

    private String formatHistory(List<Message> history, String botName) {
        if (history == null || history.isEmpty()) return "[Start of conversation]";
        return history.stream()
                .map(m -> (m.isFromUser() ? "Detective" : botName) + ": " + m.getText())
                .collect(Collectors.joining("\n"));
    }

    private String formatEvents(List<TimelineEvent> events) {
        if (events.isEmpty()) return "You did not witness any specific events.";
        return events.stream()
                .map(e -> "- At " + e.time() + " in " + e.location() + ": " + e.event())
                .collect(Collectors.joining("\n"));
    }
}