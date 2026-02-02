package com.exposure.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // 1. Важно: .cors() без параметров ищет бин с именем corsConfigurationSource
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем pre-flight запросы (OPTIONS) для всех
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/register", "/api/login", "/api/main", "/api/main/bots", "/api/main/missions",
                                "/api/game/start", "/api/game/question", "/api/game/choice", "/api/game/status/{sessionId}",
                                "/api/game/mission/{sessionId}",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**",
                                "/explorer/**").permitAll()
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Если используете JWT/Token, убедитесь, что сессии STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 2. Указываем точный origin (без "*" в конце)
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // 3. Стандартные методы
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 4. Разрешаем все заголовки, включая Authorization
        config.setAllowedHeaders(List.of("*"));

        // 5. Важно для работы с куками или заголовками авторизации
        config.setAllowCredentials(true);

        // 6. Кэширование pre-flight запроса на 1 час (улучшает производительность)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
