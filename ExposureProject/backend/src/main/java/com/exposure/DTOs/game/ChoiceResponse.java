package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChoiceResponse {
    public Boolean isLiar;
    public Long botId;

    public ChoiceResponse(Boolean isLiar, Long botId) {
        this.isLiar = isLiar;
        this.botId = botId;
    }
}
