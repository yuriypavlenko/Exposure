package com.exposure.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


/*
    Game session model
 */



/*
TODO: Добавь в игровю сессию количество доступных вопросов! 
P.s. инициазировать их в конструкторе.
*/

@Getter
@Entity
@Table(name = "sessions")
@NoArgsConstructor
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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

    public GameSession(User user, List<Bot> bots) {
        this.user = user;
        this.bots = bots;
    }
}
