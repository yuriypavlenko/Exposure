package com.exposure.controllers;

import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.GameSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/game")
public class GameController {
    BotResponseInterface botResponseInterface;

    // TODO: Отправлять тут стартовые данные про игровую сессию по запросу
    @PostMapping("/start")
    public String getPage() {
        // Инициализация сессии
        // TODO: создать в бд ботов и мокать просто через айди
        // TODO: Кидать пользователя через токен на веб

        GameSession gameSession = new GameSession();

        // Отправка DTO фронту
        return null;
    }

    // TODO: На вопрос он спрашивает у бота через
    //  клиент оллама и отправляет назад ответ
    @PostMapping("/question")
    public void onQuestion() {

    }

    // TODO: Когда игрок выбирает бота которому верит происходит проверка
    //  и вывод результата
    @PostMapping
    public void onChoice() {

    }

    // TODO: Обработка завершенния игровой сессии.
    @PostMapping("/endsession")
    public void endSession() {

    }
}
