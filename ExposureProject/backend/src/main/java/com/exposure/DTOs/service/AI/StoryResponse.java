package com.exposure.DTOs.service.AI;

import java.util.List;

public record StoryResponse(
        StoryMeta story_meta,
        List<TimelineEvent> truth_timeline,
        List<RolesData> roles_data
) {}