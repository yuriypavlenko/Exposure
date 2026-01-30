package com.exposure.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "chat_members",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<SessionMember> members = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    @OrderBy("sentAt ASC")
    private List<Message> messages = new ArrayList<>();

    public boolean hasMember(SessionMember member) {
        return members.contains(member);
    }

    public void addMessage(SessionMember sender, String text) {
        Message message = new Message();
        message.setChat(this);
        message.setSender(sender);
        message.setText(text);
        message.setSentAt(LocalDateTime.now());

        this.messages.add(message);
    }
}
