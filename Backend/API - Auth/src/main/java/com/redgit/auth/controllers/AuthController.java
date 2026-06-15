package com.redgit.auth.controllers;

import com.redgit.auth.controllers.DTO.LoginRequestDTO;
import com.redgit.auth.controllers.DTO.RegisterRequestDTO;
import com.redgit.auth.controllers.DTO.ResponseDTO;
import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.entity.UserRole;
import com.redgit.auth.infrastructure.redis.RateLimitService;
import com.redgit.auth.infrastructure.repository.UserRepository;
import com.redgit.auth.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO body){
        String email = body.email();

        if (rateLimitService.isBlocked(email)) {
            long timeRemaining = rateLimitService.getBlockTimeRemaining(email);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Conta temporariamente bloqueada");
            errorResponse.put("message", "Muitas tentativas de login. Tente novamente em " + timeRemaining + " segundos");
            errorResponse.put("remainingSeconds", timeRemaining);
            errorResponse.put("blocked", true);

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(errorResponse);
        }

        User user = this.repository.findByEmail(email)
                .orElseThrow(() -> {
                    rateLimitService.checkAndBlock(email);
                    int remaining = rateLimitService.getRemainingAttempts(email);

                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Usuário não identificado. Tentativas restantes: " + remaining
                    );
                });

        if(passwordEncoder.matches(body.password(), user.getPassword())) {
            rateLimitService.resetAttempts(email);

            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }

        boolean wasBlocked = rateLimitService.checkAndBlock(email);
        int remaining = rateLimitService.getRemainingAttempts(email);

        if (wasBlocked) {
            long blockTime = rateLimitService.getBlockTimeRemaining(email);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Conta bloqueada");
            errorResponse.put("message", "Excedeu o limite de tentativas. Conta bloqueada por " + blockTime + " segundos");
            errorResponse.put("remainingSeconds", blockTime);
            errorResponse.put("blocked", true);

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(errorResponse);
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Senha incorreta");
        errorResponse.put("message", "Senha incorreta. Tentativas restantes: " + remaining);
        errorResponse.put("remainingAttempts", remaining);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody @Valid RegisterRequestDTO body){
        Optional<User> existingUser = this.repository.findByEmail(body.email());

        if(existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            newUser.setRole(UserRole.USER);
            newUser.setEnabled(true);
            newUser.setAccountNonLocked(true);
            this.repository.save(newUser);

            String token = this.tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> registerAdmin(@RequestBody @Valid RegisterRequestDTO body){
        Optional<User> existingUser = this.repository.findByEmail(body.email());

        if(existingUser.isEmpty()) {
            User newAdmin = new User();
            newAdmin.setPassword(passwordEncoder.encode(body.password()));
            newAdmin.setEmail(body.email());
            newAdmin.setName(body.name());
            newAdmin.setRole(UserRole.ADMIN);
            newAdmin.setEnabled(true);
            newAdmin.setAccountNonLocked(true);
            this.repository.save(newAdmin);

            String token = this.tokenService.generateToken(newAdmin);
            return ResponseEntity.ok(new ResponseDTO(newAdmin.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Adiciona token à blacklist
            tokenService.blacklistToken(token);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout realizado com sucesso");

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest()
                .body(Map.of("error", "Token não fornecido"));
    }
}