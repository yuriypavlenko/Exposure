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
            validateStory(response, totalRoles);
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


    /*
    Нужно проверить столько ли ролей сколько указано в total_roles в данной структуре.
     */
    private void validateStory(StoryResponse story, int total_roles) {

        if (story.roles_data().size() != total_roles) {
            throw new IllegalStateException(
                    "Expected " + total_roles + " roles, but found " + story.roles_data().size()
            );
        }

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

    private String storyJsonSchema() {
        return """
        OUTPUT CONTRACT (STRICT):
        
        You must return a SINGLE valid JSON object.
        Any text outside JSON is FORBIDDEN.
        
        STRUCTURE:
        
        1. story_meta (object)
           - description: short neutral description of the incident.
        
        2. truth_timeline (array)
           CRITICAL STRUCTURE.
           Represents objective reality of what truly happened.
        
           Each entry describes a concrete moment in time.
           Events must be descriptive and neutral.
           Events MUST NOT reference role identifiers directly inside text.
        
           Each item:
           {
             "time": "HH:mm",
             "event": "Objective description of what occurred.",
             "location": "Physical place where it happened.",
             "witnesses": ["role1", "role2"]
           }
        
           RULES:
           - Timeline must be chronological.
           - Events should include movements, sounds, interactions, absences, changes.
           - Avoid conclusions or interpretations.
           - Do NOT reveal guilt directly.
           - The system will later convert this timeline into role-specific perspectives.
        
        3. roles_data (array)
        
           IMPORTANT:
           - Number of roles MUST equal total_roles requested by user.
           - Exactly ONE role must have isGuilty = true.
           - All others must have isGuilty = false.
        
           Each role:
        
           {
             "role": "role1",
             "role_description": "Occupation or narrative position",
             "isGuilty": boolean,
             "motive": "Reason for possible involvement (empty string allowed for innocents)",
             "alibi": "What the role CLAIMS they were doing at the time",
             "actual": "What the role was actually doing"
           }
        
        RULES:
        - role identifiers must be exactly: role1, role2, role3, ...
        - Do NOT include character names.
        - Do NOT include solution field.
        - Do NOT include clues field.
        - Do NOT include markdown.
        - All strings must be meaningful and non-null.
        """;
    }
}
