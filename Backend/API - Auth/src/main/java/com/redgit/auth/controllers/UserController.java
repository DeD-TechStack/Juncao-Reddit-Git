package com.redgit.auth.controllers;

import com.redgit.auth.controllers.DTO.UpdateUserDTO;
import com.redgit.auth.controllers.DTO.UserDTO;
import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        User fullUser = userService.findByEmail(user.getEmail());
        return ResponseEntity.ok(new UserDTO(fullUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id)")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(new UserDTO(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.isOwner(#id)")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserDTO dto) {
        User user = userService.updateProfile(id, dto);
        return ResponseEntity.ok(new UserDTO(user));
    }
}
