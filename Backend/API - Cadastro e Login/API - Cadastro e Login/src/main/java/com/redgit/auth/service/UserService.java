package com.redgit.auth.service;

import com.redgit.auth.controllers.DTO.UpdateUserDTO;
import com.redgit.auth.controllers.DTO.UserDTO;
import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.entity.UserRole;
import com.redgit.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
    }

public User findById(UUID id) {
        return userRepository.findById(id.toString())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
    }

    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDTO::new);
    }

    @Transactional
    public User updateProfile(UUID id, UpdateUserDTO dto) {
        User user = findById(id);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            userRepository.findByEmail(dto.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Email já está em uso"
                            );
                        }
                    });
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public User changeRole(UUID id, UserRole newRole) {
        User user = findById(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User currentUser) {
            if (currentUser.getId().equals(user.getId()) && user.getRole() == UserRole.ADMIN) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Você não pode remover sua própria role de ADMIN"
                );
            }
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);

        // Impedir que ADMIN delete a si mesmo
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User currentUser) {
            if (currentUser.getId().equals(user.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Você não pode deletar sua própria conta"
                );
            }
        }

        userRepository.delete(user);
    }

    @Transactional
    public void lockAccount(UUID id) {
        User user = findById(id);

        if (user.getRole() == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível bloquear conta de ADMIN"
            );
        }

        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Transactional
    public void unlockAccount(UUID id) {
        User user = findById(id);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableAccount(UUID id) {
        User user = findById(id);

        if (user.getRole() == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível desativar conta de ADMIN"
            );
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enableAccount(UUID id) {
        User user = findById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public long countByRole(UserRole role) {
        return userRepository.findByRole(role).size();
    }
}
