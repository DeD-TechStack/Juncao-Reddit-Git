package com.redgit.profile.repository;

import com.redgit.profile.infrastructure.entities.Profile;
import com.redgit.profile.infrastructure.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ProfileRepository Tests")
public class ProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProfileRepository profileRepository;

    private UUID userId;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testProfile = new Profile();
        testProfile.setUserId(userId);
        testProfile.setUsername("testuser");
        testProfile.setDisplayName("Test User");
        testProfile.setBio("Test bio");
        testProfile.setPublic(true);
    }

    @Test
    @DisplayName("Deve salvar um perfil com sucesso")
    void save_shouldPersistProfile() {
        // Act
        Profile saved = profileRepository.save(testProfile);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getDisplayName()).isEqualTo("Test User");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve encontrar perfil por userId")
    void findByUserId_whenExists_shouldReturnProfile() {
        // Arrange
        entityManager.persist(testProfile);
        entityManager.flush();

        // Act
        Optional<Profile> found = profileRepository.findByUserId(userId);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Deve retornar vazio quando userId não existe")
    void findByUserId_whenNotExists_shouldReturnEmpty() {
        // Act
        Optional<Profile> found = profileRepository.findByUserId(UUID.randomUUID());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar perfil por username")
    void findByUsername_whenExists_shouldReturnProfile() {
        // Arrange
        entityManager.persist(testProfile);
        entityManager.flush();

        // Act
        Optional<Profile> found = profileRepository.findByUsername("testuser");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Deve retornar vazio quando username não existe")
    void findByUsername_whenNotExists_shouldReturnEmpty() {
        // Act
        Optional<Profile> found = profileRepository.findByUsername("nonexistent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se username existe")
    void existsByUsername_whenExists_shouldReturnTrue() {
        // Arrange
        entityManager.persist(testProfile);
        entityManager.flush();

        // Act
        boolean exists = profileRepository.existsByUsername("testuser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando username não existe")
    void existsByUsername_whenNotExists_shouldReturnFalse() {
        // Act
        boolean exists = profileRepository.existsByUsername("nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se userId existe")
    void existsByUserId_whenExists_shouldReturnTrue() {
        // Arrange
        entityManager.persist(testProfile);
        entityManager.flush();

        // Act
        boolean exists = profileRepository.existsByUserId(userId);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando userId não existe")
    void existsByUserId_whenNotExists_shouldReturnFalse() {
        // Act
        boolean exists = profileRepository.existsByUserId(UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve garantir que username é único")
    void save_withDuplicateUsername_shouldFail() {
        // Arrange
        entityManager.persist(testProfile);
        entityManager.flush();

        Profile duplicate = new Profile();
        duplicate.setUserId(UUID.randomUUID());
        duplicate.setUsername("testuser"); // mesmo username
        duplicate.setDisplayName("Another User");

        // Act & Assert
        try {
            profileRepository.save(duplicate);
            entityManager.flush();
            assertThat(false).isTrue(); // Não deveria chegar aqui
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Deve garantir que userId é único")
    void save_withDuplicateUserId_shouldFail() {
        // Arrange
        entityManager.persist(testProfile);
        entityManager.flush();

        Profile duplicate = new Profile();
        duplicate.setUserId(userId); // mesmo userId
        duplicate.setUsername("anotheruser");
        duplicate.setDisplayName("Another User");

        // Act & Assert
        try {
            profileRepository.save(duplicate);
            entityManager.flush();
            assertThat(false).isTrue(); // Não deveria chegar aqui
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Deve atualizar perfil existente")
    void save_updateExisting_shouldUpdateProfile() {
        // Arrange
        Profile saved = entityManager.persist(testProfile);
        entityManager.flush();

        // Act
        saved.setDisplayName("Updated Name");
        saved.setBio("Updated bio");
        Profile updated = profileRepository.save(saved);
        entityManager.flush();

        // Assert
        Profile found = entityManager.find(Profile.class, saved.getId());
        assertThat(found.getDisplayName()).isEqualTo("Updated Name");
        assertThat(found.getBio()).isEqualTo("Updated bio");
    }

    @Test
    @DisplayName("Deve deletar perfil por ID")
    void deleteById_shouldRemoveProfile() {
        // Arrange
        Profile saved = entityManager.persist(testProfile);
        entityManager.flush();
        UUID profileId = saved.getId();

        // Act
        profileRepository.deleteById(profileId);
        entityManager.flush();

        // Assert
        Profile found = entityManager.find(Profile.class, profileId);
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("Deve salvar perfil com social links JSON")
    void save_withSocialLinks_shouldPersistJson() {
        // Arrange
        testProfile.getSocialLinks().put("github", "testuser");
        testProfile.getSocialLinks().put("linkedin", "test-user");

        // Act
        Profile saved = profileRepository.save(testProfile);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Profile found = profileRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSocialLinks()).hasSize(2);
        assertThat(found.getSocialLinks().get("github")).isEqualTo("testuser");
        assertThat(found.getSocialLinks().get("linkedin")).isEqualTo("test-user");
    }

    @Test
    @DisplayName("Deve salvar perfil com isPublic false")
    void save_withPrivateProfile_shouldPersist() {
        // Arrange
        testProfile.setPublic(false);

        // Act
        Profile saved = profileRepository.save(testProfile);
        entityManager.flush();

        // Assert
        Profile found = profileRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.isPublic()).isFalse();
    }

    @Test
    @DisplayName("Deve contar o total de perfis")
    void count_shouldReturnCorrectNumber() {
        // Arrange
        entityManager.persist(testProfile);

        Profile another = new Profile();
        another.setUserId(UUID.randomUUID());
        another.setUsername("another");
        another.setDisplayName("Another");
        entityManager.persist(another);

        entityManager.flush();

        // Act
        long count = profileRepository.count();

        // Assert
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}