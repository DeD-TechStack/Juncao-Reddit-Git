package com.redgit.profile.service;

import com.redgit.profile.controller.dto.UpdateProfileDTO;
import com.redgit.profile.infrastructure.entities.Profile;
import com.redgit.profile.infrastructure.repository.ProfileRepository;
import com.redgit.profile.infrastructure.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private Profile testProfile;
    private String testEmail;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testEmail = "test@example.com";

        testProfile = new Profile();
        testProfile.setId(UUID.randomUUID());
        testProfile.setUserId(userId);
        testProfile.setUsername("testuser");
        testProfile.setDisplayName("Test User");
        testProfile.setBio("Test bio");
        testProfile.setPublic(true);
    }

    // ========== findByUserId Tests ==========

    @Test
    @DisplayName("Deve encontrar perfil por userId quando existe")
    void findByUserId_whenExists_shouldReturnProfile() {
        // Arrange
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // Act
        Profile result = profileService.findByUserId(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(profileRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando userId não existe")
    void findByUserId_whenNotExists_shouldThrowException() {
        // Arrange
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> profileService.findByUserId(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Perfil não encontrado")
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(profileRepository, times(1)).findByUserId(userId);
    }

    // ========== findByUsername Tests ==========

    @Test
    @DisplayName("Deve encontrar perfil por username quando existe")
    void findByUsername_whenExists_shouldReturnProfile() {
        // Arrange
        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(testProfile));

        // Act
        Profile result = profileService.findByUsername("testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(profileRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Deve lançar exceção quando username não existe")
    void findByUsername_whenNotExists_shouldThrowException() {
        // Arrange
        when(profileRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> profileService.findByUsername("nonexistent"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Perfil não encontrado");

        verify(profileRepository, times(1)).findByUsername("nonexistent");
    }

    // ========== createProfile Tests ==========

    @Test
    @DisplayName("Deve criar perfil com sucesso")
    void createProfile_shouldCreateAndReturnProfile() {
        // Arrange
        when(profileRepository.existsByUserId(userId)).thenReturn(false);
        when(profileRepository.existsByUsername(anyString())).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        Profile result = profileService.createProfile(userId, testEmail);

        // Assert
        assertThat(result).isNotNull();
        verify(profileRepository, times(1)).existsByUserId(userId);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando perfil já existe")
    void createProfile_whenAlreadyExists_shouldThrowException() {
        // Arrange
        when(profileRepository.existsByUserId(userId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> profileService.createProfile(userId, testEmail))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Perfil já existe")
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(profileRepository, times(1)).existsByUserId(userId);
        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve gerar username a partir do email")
    void createProfile_shouldGenerateUsernameFromEmail() {
        // Arrange
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        when(profileRepository.existsByUserId(userId)).thenReturn(false);
        when(profileRepository.existsByUsername(anyString())).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        profileService.createProfile(userId, "john.doe@example.com");

        // Assert
        verify(profileRepository).save(profileCaptor.capture());
        Profile captured = profileCaptor.getValue();
        assertThat(captured.getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("Deve adicionar número ao username se já existe")
    void createProfile_whenUsernameExists_shouldAddNumber() {
        // Arrange
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        when(profileRepository.existsByUserId(userId)).thenReturn(false);
        when(profileRepository.existsByUsername("test"))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        profileService.createProfile(userId, "test@example.com");

        // Assert
        verify(profileRepository).save(profileCaptor.capture());
        Profile captured = profileCaptor.getValue();
        assertThat(captured.getUsername()).isEqualTo("test1");
    }

    // ========== getOrCreateProfile Tests ==========

    @Test
    @DisplayName("Deve retornar perfil existente")
    void getOrCreateProfile_whenExists_shouldReturnExisting() {
        // Arrange
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // Act
        Profile result = profileService.getOrCreateProfile(userId, testEmail);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(profileRepository, times(1)).findByUserId(userId);
        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar perfil quando não existe")
    void getOrCreateProfile_whenNotExists_shouldCreate() {
        // Arrange
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(profileRepository.existsByUserId(userId)).thenReturn(false);
        when(profileRepository.existsByUsername(anyString())).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        Profile result = profileService.getOrCreateProfile(userId, testEmail);

        // Assert
        assertThat(result).isNotNull();
        verify(profileRepository, times(1)).findByUserId(userId);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    // ========== updateProfile Tests ==========

    @Test
    @DisplayName("Deve atualizar apenas username")
    void updateProfile_withOnlyUsername_shouldUpdateUsername() {
        // Arrange
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));
        when(profileRepository.existsByUsername("newusername")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setUsername("newusername");

        // Act
        Profile result = profileService.updateProfile(userId, dto);

        // Assert
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando username já existe")
    void updateProfile_withExistingUsername_shouldThrowException() {
        // Arrange
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));
        when(profileRepository.existsByUsername("existinguser")).thenReturn(true);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setUsername("existinguser");

        // Act & Assert
        assertThatThrownBy(() -> profileService.updateProfile(userId, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Username já está em uso")
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar múltiplos campos")
    void updateProfile_withMultipleFields_shouldUpdateAll() {
        // Arrange
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setDisplayName("New Name");
        dto.setBio("New bio");
        dto.setLocation("New Location");
        dto.setWebsite("https://example.com");
        dto.setIsPublic(false);

        Map<String, String> socialLinks = new HashMap<>();
        socialLinks.put("github", "newuser");
        dto.setSocialLinks(socialLinks);

        // Act
        profileService.updateProfile(userId, dto);

        // Assert
        verify(profileRepository).save(any(Profile.class));
    }

    // ========== uploadAvatar Tests ==========

    @Test
    @DisplayName("Deve fazer upload de avatar com sucesso")
    void uploadAvatar_shouldStoreFileAndUpdateProfile() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));
        when(fileStorageService.storeFile(file)).thenReturn("avatar-uuid.jpg");
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        Profile result = profileService.uploadAvatar(userId, file);

        // Assert
        verify(fileStorageService, times(1)).storeFile(file);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Deve deletar avatar antigo ao fazer upload de novo")
    void uploadAvatar_withExistingAvatar_shouldDeleteOld() {
        // Arrange
        testProfile.setAvatarPath("old-avatar.jpg");
        MultipartFile file = mock(MultipartFile.class);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));
        when(fileStorageService.storeFile(file)).thenReturn("new-avatar.jpg");
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // Act
        profileService.uploadAvatar(userId, file);

        // Assert
        verify(fileStorageService, times(1)).deleteFile("old-avatar.jpg");
        verify(fileStorageService, times(1)).storeFile(file);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    // ========== deleteAvatar Tests ==========

    @Test
    @DisplayName("Deve deletar avatar com sucesso")
    void deleteAvatar_shouldRemoveFileAndUpdateProfile() {
        // Arrange
        testProfile.setAvatarPath("avatar.jpg");
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // Act
        profileService.deleteAvatar(userId);

        // Assert
        verify(fileStorageService, times(1)).deleteFile("avatar.jpg");
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há avatar para deletar")
    void deleteAvatar_whenNoAvatar_shouldThrowException() {
        // Arrange
        testProfile.setAvatarPath(null);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // Act & Assert
        assertThatThrownBy(() -> profileService.deleteAvatar(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não possui avatar")
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(profileRepository, never()).save(any());
    }
}