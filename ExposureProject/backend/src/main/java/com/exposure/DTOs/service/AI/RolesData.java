package com.exposure.DTOs.service.AI;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

public record RolesData(
        String role,
        boolean isGuilty,
        @JsonSetter(nulls = Nulls.AS_EMPTY) String motive,
        String alibi,
        String actual
) {}
