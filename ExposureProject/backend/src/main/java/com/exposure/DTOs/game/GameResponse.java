package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameResponse {
    public long sessionId;
    public List<BotDTO> bots;
    public int questionsLeft;

    public GameResponse(long sessionId, List<BotDTO> bots, int questionsLeft) {
        this.sessionId = sessionId;
        this.bots = bots;
        this.questionsLeft = questionsLeft;
    }
}
