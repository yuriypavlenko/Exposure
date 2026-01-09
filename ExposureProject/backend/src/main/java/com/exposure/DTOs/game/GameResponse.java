package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameResponse {
    public Long sessionId;
    public Long userId;
    public List<BotDTO> bots;
    public int questionsLeft;

    public GameResponse(Long sessionId, Long userId, List<BotDTO> bots, int questionsLeft) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.bots = bots;
        this.questionsLeft = questionsLeft;
    }
}
