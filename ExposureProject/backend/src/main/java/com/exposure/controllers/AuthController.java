package com.exposure.controllers;

import com.exposure.DTOs.Auth.AuthRequest;
import com.exposure.DTOs.Auth.AuthResponse;
import com.exposure.models.User;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/*
Добавить потом сюда хэширование паролей через соль. Можешь взять механизм с Openchat, я там уже такое делал.
*/


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        System.out.println("register");

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        // ВАЖНО: В реальном проекте пароль нужно хешировать (BCrypt)!
        User newUser = new User(request.getUsername(), request.getPassword());
        User savedUser = userRepository.save(newUser);

        return ResponseEntity.ok(savedUser.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .map(user -> ResponseEntity.ok(new AuthResponse(user.getId(), null)))
                .orElse(ResponseEntity.status(401).body(new AuthResponse(null, "Неверные данные.")));
    }
}
