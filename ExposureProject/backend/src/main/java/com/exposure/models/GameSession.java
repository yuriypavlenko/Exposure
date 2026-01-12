package com.exposure.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;


/*
    Game session model
 */


@Getter
@Setter
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "session_id")
    private List<Chat> chats = new ArrayList<>();

    private int questionsLeft;

    private Boolean isActive;

    public GameSession(User user, List<Bot> bots, List<Bot> lyingBots, int initialQuestions) {
        this.user = user;
        this.bots = bots;
        this.lyingBots = lyingBots;
        this.questionsLeft = initialQuestions;
        this.isActive = true;
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

    public void addChat(Chat chat) {
        this.chats.add(chat);
    }
}
