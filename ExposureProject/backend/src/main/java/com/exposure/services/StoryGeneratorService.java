package com.exposure.services;

import com.exposure.DTOs.service.AI.RolesData;
import com.exposure.DTOs.service.AI.StoryResponse;
import com.exposure.models.*;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
Нужно изменить генерацию так, чтобы в истории не было конкретного персонажа.
Для этого как раз в миссии (родительском объекте грубо говоря) существуют настройки и выбираются боты. Они могут быть разные.
Это сделано для того, чтобы истории можно было не генерировать и просто подставлять ботов с их характерами.
Есть роль,
 */


@Service
@RequiredArgsConstructor
public class StoryGeneratorService {

    private final ChatClient chatClient;
    private final StoryRepository storyRepository;
    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(StoryGeneratorService.class);

    public Map<String, Object> generateStory(Mission mission, int totalRoles, int totalLyingRoles) {

        String fullPrompt = buildStoryPrompt(
                mission.getHistory_description(),
                totalRoles,
                totalLyingRoles
        );

        Prompt prompt = new Prompt(
                List.of(new UserMessage(fullPrompt))
        );

        StringBuilder contentBuilder = new StringBuilder();
        String rawResponse;

        try {
            Iterable<String> tokens = chatClient
                    .prompt(prompt)
                    .stream()
                    .content()
                    .toIterable();

            for (String token : tokens) {
                if (Thread.currentThread().isInterrupted()) {
                    logger.info("Ollama generation interrupted by user.");
                    return null;
                }
                contentBuilder.append(token);
            }

            rawResponse = contentBuilder.toString();

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

        story = storyRepository.save(story);
        return Map.of("story", story, "storyResponse", response);
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

        String roleList = IntStream.rangeClosed(1, totalRoles)
                .mapToObj(i -> "\"role" + i + "\"")
                .collect(Collectors.joining(", "));

        String rolesInfo = """
            ROLE SET (ABSOLUTE):
            
            The ONLY existing roles in this scenario are:
            [%s]
            
            This list is FINAL.
            No additional roles may exist.
            No role outside this list may appear anywhere in the output.
            
            LYING ROLES COUNT (ABSOLUTE):
            %d
            
            Exactly %d roles must have alibi ≠ actual.
            All other roles must have alibi === actual.
            """.formatted(roleList, lyingRoles, lyingRoles);


        return """
            SYSTEM ROLE:
            You are a deterministic crime scenario generator.
    
            You must strictly follow the output format contract.
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
           SYSTEM ROLE:
           You are a deterministic crime scenario generator. Return ONLY raw JSON.

           CONSTRAINTS:
           1. Roles: ONLY "role1", "role2".
           2. Guilt: Exactly one role isGuilty=true.
           3. Lying: Exactly one role has alibi != actual. The guilty role MUST be the liar.
           4. Witnesses: Use ONLY ["role1", "role2"] identifiers.

           JSON STRUCTURE (STRICT):
           {
             "story_meta": { "description": "string" },
             "truth_timeline": [
               { "time": "HH:mm", "event": "string", "location": "string", "witnesses": ["role1"] }
             ],
             "roles_data": [
               {
                 "role": "role1",
                 "role_description": "string",
                 "isGuilty": boolean,
                 "motive": "string or null",
                 "alibi": "string",
                 "actual": "string"
               }
             ]
           }

           EXAMPLE OF TRUTH_TIMELINE (Follow this format but replace the event with your own based on the mission description):
           "truth_timeline": [
             { "time": "18:00", "event": "Music starts", "location": "Ballroom", "witnesses": ["role1", "role2"] }
           ]

           IMPORTANT:
           - "truth_timeline" MUST be a JSON ARRAY, not an object.
           - Include ALL fields from the structure (location, role_description, etc.).
           - No markdown, no intro, no wrap.
           """;
    }
}
