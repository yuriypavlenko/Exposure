package com.exposure.repositories;

import com.exposure.models.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findAllByUserIdAndIsActiveTrue(Long userId);
}
