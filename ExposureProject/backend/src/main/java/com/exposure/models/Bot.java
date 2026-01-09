package com.exposure.models;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/*
    Bot Model
 */


@Getter
@Entity
@NoArgsConstructor
@Table(name = "bots")
public class Bot extends SessionMember {
    private String personality;

    private LocalDateTime createdAt;

    public Bot(String name, String personality) {
        super(name);

        this.personality = personality;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
