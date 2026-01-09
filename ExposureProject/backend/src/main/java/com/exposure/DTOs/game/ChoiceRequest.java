package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChoiceRequest {
    public Long userId;
    public Long botId;
    public Long sessionId;

    public ChoiceRequest(Long userId, Long botId, Long sessionId) {
        this.userId = userId;
        this.botId = botId;
        this.sessionId = sessionId;
    }
}
