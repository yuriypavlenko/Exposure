package com.exposure.controllers;

import com.exposure.DTOs.Auth.AuthRequest;
import com.exposure.models.User;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


// TODO: проверить сохранение пользователя
// TODO: исправить ошибки на фронте связанные с коннектиоом

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        // Проверяем, не занято ли имя
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        // ВАЖНО: В реальном проекте пароль нужно хешировать (BCrypt)!
        User newUser = new User(request.getUsername(), request.getPassword());
        User savedUser = userRepository.save(newUser);

        return ResponseEntity.ok(savedUser.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> user.getPassword().equals(request.getPassword())) // Сравнение паролей
                .map(user -> ResponseEntity.ok(user.getId())) // Возвращаем ID
                .orElse(ResponseEntity.status(401).build()); // 401 если неверно
    }
}
