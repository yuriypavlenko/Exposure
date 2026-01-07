package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionRequest {
    public Long userId;
    public Long botId;
    public Long sessionId;
    public String question;

    public QuestionRequest(Long userId, Long botId, Long sessionId, String question) {
        this.userId = userId;
        this.botId = botId;
        this.sessionId = sessionId;
        this.question = question;
    }
}
