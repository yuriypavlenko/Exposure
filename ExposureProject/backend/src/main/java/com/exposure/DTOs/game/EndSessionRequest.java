package com.exposure.DTOs.game;

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
