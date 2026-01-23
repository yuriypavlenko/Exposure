package com.exposure.services;

import com.exposure.DTOs.service.AI.RolesData;
import com.exposure.DTOs.service.AI.StoryResponse;
import com.exposure.models.Mission;
import com.exposure.models.Story;
import com.exposure.repositories.StoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.util.List;

/*
Нужно изменить генерацию так, чтобы в истории не было конкретного персонажа.
Для этого как раз в миссии (родительском объекте грубо говоря) существуют настройки и выбираются боты. Они могут быть разные.
Это сделано для того, чтобы истории можно было не генерировать и просто подставлять ботов с их характерами.
Есть роль,
 */


@Service
@RequiredArgsConstructor
public class MissionService {

    private final ChatClient chatClient;
    private final StoryRepository storyRepository;
    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(MissionService.class);

    public Story generateStory(Mission mission, int totalRoles, int totalLyingRoles) {

        String fullPrompt = buildStoryPrompt(
                mission.getHistory_description(),
                totalRoles,
                totalLyingRoles
        );

        Prompt prompt = new Prompt(
                List.of(new UserMessage(fullPrompt))
        );

        String rawResponse;

        try {
            rawResponse = chatClient
                    .prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {

            logger.error("LLM call failed");
            logger.error("Prompt:\n{}", fullPrompt, e);

            throw new IllegalStateException("LLM call failed", e);
        }

        String cleanedJson;

        try {
            cleanedJson = extractJson(rawResponse);
        } catch (Exception e) {
            logger.error("Failed to extract JSON from LLM response");
            logger.error("Raw response:\n{}", rawResponse);
            throw e;
        }

        StoryResponse response;

        try {
            response = objectMapper.readValue(cleanedJson, StoryResponse.class);
        } catch (Exception e) {

            logger.error("Invalid LLM JSON");
            logger.error("Prompt:\n{}", fullPrompt);
            logger.error("Raw response:\n{}", cleanedJson);

            throw new IllegalStateException(
                    "LLM returned invalid structure",
                    e
            );
        }

        try {
            validateStory(response);
        } catch (Exception e) {
            logger.error("Invalid validation");
            logger.error("Prompt:\n{}", fullPrompt);
            logger.error("Raw response:\n{}", rawResponse);
            throw new RuntimeException(e);
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize StoryResponse", e);
        }

        Story story = new Story();
        story.setMission(mission);
        story.setGeneratedStoryJson(json);

        return storyRepository.save(story);
    }

    private String extractJson(String raw) {

        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalStateException(
                    "LLM response does not contain valid JSON object."
            );
        }

        return raw.substring(start, end + 1);
    }

    private void validateStory(StoryResponse story) {
        long guiltyCount = story.roles_data()
                .stream()
                .filter(RolesData::isGuilty)
                .count();

        if (guiltyCount != 1) {
            throw new IllegalStateException(
                    "Story must contain exactly one guilty character"
            );
        }
    }

    private String buildStoryPrompt(
            String missionDescription,
            int totalRoles,
            int lyingRoles
    ) {

        String rolesInfo =
                "Total roles: " + totalRoles + ". " +
                        "Roles must be named strictly as role1, role2, ..., role" + totalRoles + ". " +
                        "Exactly " + lyingRoles + " roles must lie in their alibi.";

        return """
            SYSTEM ROLE:
            You are a deterministic crime scenario generator.
    
            You must strictly follow the output contract.
            Any violation is considered a fatal error.
    
            MISSION DESCRIPTION:
            %s
    
            ROLES:
            %s
    
            LOGIC RULES:
            - Exactly ONE role is guilty (isGuilty = true)
            - Guilty role must be among those who lie
            - All other roles tell the truth
            - Timeline must be logically consistent
            - Clues must confirm the real version of events
            - Alibis may conflict with reality only for lying roles
    
            OUTPUT RULES:
            - Return ONLY valid JSON
            - No markdown
            - No comments
            - No text before or after JSON
    
            OUTPUT CONTRACT:
            %s
            """.formatted(
                    missionDescription,
                    rolesInfo,
                    storyJsonSchema()
        );
    }


    /*
    TODO
    В description и solution нужно тоже указывать.
    Добавить описание роли в истории после роли в roles_data
     */
    private String storyJsonSchema() {
        return """
            OUTPUT CONTRACT (STRICT):
        
            You must return a SINGLE valid JSON object.
            Any text outside JSON is FORBIDDEN.
        
            STRUCTURE:
        
            1. story_meta (object)
               - description: short description of the incident
               - solution: textual explanation of who is guilty and why
                 (DO NOT return role id only)
        
            2. truth_timeline (array)
               Chronological list of real events.
               Each item:
               - time: string in format HH:mm
               - event: what actually happened
               - location: where it happened
               - witnesses: array of role identifiers (example: "role1")
        
            3. roles_data (array)
               List of abstract roles involved in the incident.
        
               Each role MUST contain ALL fields:
               - role: identifier ("role1", "role2", ...)
               - isGuilty: boolean
               - motive: reason for committing or potentially committing the crime (motive must never be null, use empty string instead)
               - alibi: what this role claims
               - actual: what this role actually did
        
            4. clues (array)
               Physical or logical evidence.
               Each item:
               - type
               - description
               - found_at (location or role id)
        
            RULES:
            - Use ONLY abstract roles (role1, role2, role3...)
            - Exactly ONE role must have isGuilty = true
            - Guilty role MUST lie in alibi
            - All non-guilty roles MUST tell the truth
            - Do NOT use names
            - Do NOT mention bots
            - Do NOT use markdown
            - Do NOT add explanations
            - Output JSON only
            """;
    }
}
