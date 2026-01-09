package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChoiceResponse {
    public Boolean isCorrect;
    public Long botId;

    public ChoiceResponse(Boolean isCorrect, Long botId) {
        this.isCorrect = isCorrect;
        this.botId = botId;
    }
}
