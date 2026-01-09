package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndSessionRequest {
    public Long userId;
    public Long sessionId;

    public EndSessionRequest(Long userId, Long sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }
}
