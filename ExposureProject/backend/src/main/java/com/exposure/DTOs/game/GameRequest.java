package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameRequest {
    public String userId;
    public List<Long> selectedBotId;

    public GameRequest(String userId, List<Long> selectedBotId) {
        this.userId = userId;
        this.selectedBotId = selectedBotId;
    }
}
