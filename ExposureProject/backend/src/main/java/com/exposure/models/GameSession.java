package com.exposure.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


/*
    Game session model
 */


@Getter
@Entity
@Table(name = "sessions")
@NoArgsConstructor
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "session_bots",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "bot_id")
    )
    private List<Bot> bots;

    @ManyToMany
    @JoinTable(
            name = "session_lying_bots",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "bot_id")
    )
    private List<Bot> lyingBots;

    private int questionsLeft;

    public GameSession(User user, List<Bot> bots, List<Bot> lyingBots, int initialQuestions) {
        this.user = user;
        this.bots = bots;
        this.lyingBots = lyingBots;
        this.questionsLeft = initialQuestions;
    }

    public boolean isBotLying(Long botId) {
        return lyingBots.stream()
                .anyMatch(bot -> bot.getId().equals(botId));
    }

    public int decreaseQuestionLeft() {
        if (this.questionsLeft > 0) {
            this.questionsLeft--;
        }
        return this.questionsLeft;
    }
}
