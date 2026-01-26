package com.exposure.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "session_bot_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionBotRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private GameSession session;

    @ManyToOne
    @JoinColumn(name = "bot_id")
    private Bot bot;

    private String roleIdentifier;

    private boolean isGuilty;
}
