package com.exposure.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/*
    Session member model
 */


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "session_members")
public abstract class SessionMember {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public SessionMember(String name) {
        this.name = name;
    }
}
