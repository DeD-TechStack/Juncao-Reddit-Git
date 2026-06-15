package com.redgit.auth.service;

import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;
    private String email;

    @BeforeEach
    void setUp() {
        email = "usuario@teste.com";

        user = new User();
        user.setEmail(email);
        user.setPassword("senhaEncriptada123");
        user.setName("Usuario Teste");
    }

    // ==================== TESTES DE loadUserByUsername ====================

    @Test
    @DisplayName("Deve carregar usuário por email quando usuário existe")
    void deveCarregarUsuarioPorEmailQuandoExiste() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo(email);
        assertThat(resultado.getPassword()).isEqualTo("senhaEncriptada123");
        assertThat(resultado.getAuthorities()).isEmpty();

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existe")
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        String emailInexistente = "inexistente@teste.com";
        when(userRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(emailInexistente))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuário não encontrado");

        verify(userRepository, times(1)).findByEmail(emailInexistente);
    }

    @Test
    @DisplayName("Deve retornar UserDetails com authorities vazia")
    void deveRetornarUserDetailsComAuthoritiesVazia() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(resultado.getAuthorities()).isNotNull();
        assertThat(resultado.getAuthorities()).hasSize(0);

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve retornar UserDetails com senha do usuário")
    void deveRetornarUserDetailsComSenhaDoUsuario() {
        // Arrange
        String senhaEsperada = "outraSenhaEncriptada456";
        user.setPassword(senhaEsperada);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(resultado.getPassword()).isEqualTo(senhaEsperada);

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve buscar usuário usando o email fornecido como username")
    void deveBuscarUsuarioUsandoEmailComoUsername() {
        // Arrange
        String emailDiferente = "outro@teste.com";
        User outroUser = new User();
        outroUser.setEmail(emailDiferente);
        outroUser.setPassword("senha123");

        when(userRepository.findByEmail(emailDiferente)).thenReturn(Optional.of(outroUser));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(emailDiferente);

        // Assert
        assertThat(resultado.getUsername()).isEqualTo(emailDiferente);
        verify(userRepository, times(1)).findByEmail(emailDiferente);
        verify(userRepository, never()).findByEmail(email);
    }

    @Test
    @DisplayName("Deve retornar UserDetails do tipo Spring Security User")
    void deveRetornarUserDetailsDoTipoSpringSecurityUser() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(resultado).isInstanceOf(org.springframework.security.core.userdetails.User.class);

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve verificar que UserDetails retornado está habilitado por padrão")
    void deveVerificarQueUserDetailsEstaHabilitadoPorPadrao() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(resultado.isEnabled()).isTrue();
        assertThat(resultado.isAccountNonExpired()).isTrue();
        assertThat(resultado.isAccountNonLocked()).isTrue();
        assertThat(resultado.isCredentialsNonExpired()).isTrue();

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve processar email com caracteres especiais")
    void deveProcessarEmailComCaracteresEspeciais() {
        // Arrange
        String emailEspecial = "usuario+teste@exemplo.com.br";
        User userEspecial = new User();
        userEspecial.setEmail(emailEspecial);
        userEspecial.setPassword("senha");

        when(userRepository.findByEmail(emailEspecial)).thenReturn(Optional.of(userEspecial));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(emailEspecial);

        // Assert
        assertThat(resultado.getUsername()).isEqualTo(emailEspecial);
        verify(userRepository, times(1)).findByEmail(emailEspecial);
    }

    @Test
    @DisplayName("Deve processar email em maiúsculas se fornecido")
    void deveProcessarEmailEmMaiusculas() {
        // Arrange
        String emailMaiusculo = "USUARIO@TESTE.COM";
        User userMaiusculo = new User();
        userMaiusculo.setEmail(emailMaiusculo);
        userMaiusculo.setPassword("senha");

        when(userRepository.findByEmail(emailMaiusculo)).thenReturn(Optional.of(userMaiusculo));

        // Act
        UserDetails resultado = customUserDetailsService.loadUserByUsername(emailMaiusculo);

        // Assert
        assertThat(resultado.getUsername()).isEqualTo(emailMaiusculo);
        verify(userRepository, times(1)).findByEmail(emailMaiusculo);
    }

    @Test
    @DisplayName("Deve lançar exceção com mensagem específica quando usuário não encontrado")
    void deveLancarExcecaoComMensagemEspecifica() {
        // Arrange
        String emailInexistente = "nao.existe@teste.com";
        when(userRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(emailInexistente))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(userRepository, times(1)).findByEmail(emailInexistente);
    }

    @Test
    @DisplayName("Deve chamar repository apenas uma vez por busca")
    void deveChamarRepositoryApenasUmaVez() {
        // Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        customUserDetailsService.loadUserByUsername(email);

        // Assert
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }
}
