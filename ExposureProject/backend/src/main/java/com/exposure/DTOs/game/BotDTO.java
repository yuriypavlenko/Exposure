package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

// TODO: Перенести этот файл в более базовую часть, т.к. используется в нескольких модулях.

@Getter
@Setter
public class BotDTO {
    public long id;
    public String name;

    public BotDTO(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
