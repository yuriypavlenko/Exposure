package com.exposure.repositories;

import com.exposure.models.Bot;
import com.exposure.models.GameSession;
import com.exposure.models.SessionBotRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionBotRoleRepository extends JpaRepository<SessionBotRole, Long> {
    Optional<SessionBotRole> findBySessionAndBot(GameSession session, Bot bot);
}
