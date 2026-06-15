package com.redgit.auth.service;

import com.redgit.auth.controllers.DTO.UpdateUserDTO;
import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.entity.UserRole;
import com.redgit.auth.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
    }

    @Test
    @DisplayName("findByEmail retorna usuário quando existe")
    void findByEmail_UserExists_ReturnsUser() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("user@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@test.com");
        verify(userRepository, times(1)).findByEmail("user@test.com");
    }

    @Test
    @DisplayName("findByEmail lança exceção quando não existe")
    void findByEmail_UserNotExists_ThrowsException() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("notfound@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("updateProfile atualiza nome do usuário")
    void updateProfile_UpdatesName() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setName("New Name");

        when(userRepository.findById(userId.toString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateProfile(userId, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changeRole muda role do usuário")
    void changeRole_ChangesRole() {
        when(userRepository.findById(userId.toString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.changeRole(userId, UserRole.ADMIN);

        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("lockAccount bloqueia conta de USER")
    void lockAccount_LocksUserAccount() {
        when(userRepository.findById(userId.toString())).thenReturn(Optional.of(user));

        userService.lockAccount(userId);

        assertThat(user.isAccountNonLocked()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("lockAccount não bloqueia conta de ADMIN")
    void lockAccount_DoesNotLockAdminAccount() {
        user.setRole(UserRole.ADMIN);
        when(userRepository.findById(userId.toString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.lockAccount(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não é possível bloquear conta de ADMIN");
    }

    @Test
    @DisplayName("delete remove usuário")
    void delete_RemovesUser() {
        when(userRepository.findById(userId.toString())).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("countUsers retorna total de usuários")
    void countUsers_ReturnsTotal() {
        when(userRepository.count()).thenReturn(10L);

        long result = userService.countUsers();

        assertThat(result).isEqualTo(10L);
    }
}
