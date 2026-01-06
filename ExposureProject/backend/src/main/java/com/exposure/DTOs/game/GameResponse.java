package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameResponse {
    public long sessionId;
    public List<BotDTO> bots;

    public GameResponse(long sessionId, List<BotDTO> bots) {
        this.sessionId = sessionId;
        this.bots = bots;
    }
}
