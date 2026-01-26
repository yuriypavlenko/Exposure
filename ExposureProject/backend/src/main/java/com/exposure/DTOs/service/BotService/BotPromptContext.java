package com.exposure.DTOs.service.BotService;

import com.exposure.DTOs.service.AI.TimelineEvent;

import java.util.List;

public record BotPromptContext(
        String botName,
        String personality,
        String missionDescription,
        String roleDescription,
        String factToStickTo,
        String motive,
        List<TimelineEvent> relevantEvents
) {}