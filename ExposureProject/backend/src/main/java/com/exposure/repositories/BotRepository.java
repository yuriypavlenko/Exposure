package com.exposure.repositories;

import com.exposure.models.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotRepository extends JpaRepository<Bot, Long> {
    Optional<Bot> findByName(String name);
    Optional<Bot> findBotById(Long id);
}
