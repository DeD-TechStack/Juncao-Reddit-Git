package com.redgit.profile.service;

import com.redgit.profile.controller.dto.UpdateProfileDTO;
import com.redgit.profile.infrastructure.entities.Profile;
import com.redgit.profile.infrastructure.redis.RedisService;
import com.redgit.profile.infrastructure.repository.ProfileRepository;
import com.redgit.profile.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final FileStorageService fileStorageService;
    private final RedisService redisService;

    @Value("${cache.profile.ttl:300}")
    private long profileCacheTTL;

    public Profile getOrCreateProfile(UUID userId, String email) {
        return profileRepository.findByUserId(userId)
                .orElseGet(() -> createProfile(userId, email));
    }

    public Profile findByUserId(UUID userId) {
        String cacheKey = "user:" + userId.toString();
        Profile cached = redisService.get(cacheKey, Profile.class);

        if (cached != null) {
            log.debug("Cache HIT - Perfil userId: {}", userId);
            return cached;
        }

        log.debug("Cache MISS - Buscando do banco: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil não encontrado"
                ));

        redisService.set(cacheKey, profile, profileCacheTTL);
        log.debug("Perfil cacheado: {} (TTL: {}s)", userId, profileCacheTTL);

        return profile;
    }

    public Profile findByUsername(String username) {
        String cacheKey = "public:" + username;
        Profile cached = redisService.get(cacheKey, Profile.class);

        if (cached != null) {
            log.debug("Cache HIT - Perfil público: {}", username);
            return cached;
        }

        log.debug("Cache MISS - Buscando do banco: {}", username);

        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil não encontrado"
                ));

        redisService.set(cacheKey, profile, profileCacheTTL);
        log.debug("Perfil público cacheado: {} (TTL: {}s)", username, profileCacheTTL);

        return profile;
    }

    @Transactional
    public Profile createProfile(UUID userId, String email) {
        // Verificar se já existe
        if (profileRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Perfil já existe para este usuário"
            );
        }

        String initialUsername = generateUsernameFromEmail(email);

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setUsername(initialUsername);
        profile.setDisplayName(email.split("@")[0]); // Nome inicial do email
        profile.setPublic(true);

        return profileRepository.save(profile);
    }

    @Transactional
    public Profile updateProfile(UUID userId, UpdateProfileDTO dto) {
        Profile profile = findByUserId(userId);

        if (dto.getUsername() != null && !dto.getUsername().equals(profile.getUsername())) {
            if (profileRepository.existsByUsername(dto.getUsername())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Username já está em uso"
                );
            }

            if (profile.getUsername() != null) {
                redisService.invalidateProfile(profile.getUsername());
                log.info("Cache invalidado para username antigo: {}", profile.getUsername());
            }

            profile.setUsername(dto.getUsername());
        }

        if (dto.getDisplayName() != null) {
            profile.setDisplayName(dto.getDisplayName());
        }

        if (dto.getBio() != null) {
            profile.setBio(dto.getBio());
        }

        if (dto.getLocation() != null) {
            profile.setLocation(dto.getLocation());
        }

        if (dto.getWebsite() != null) {
            profile.setWebsite(dto.getWebsite());
        }

        if (dto.getSocialLinks() != null) {
            profile.setSocialLinks(dto.getSocialLinks());
        }

        if (dto.getIsPublic() != null) {
            profile.setPublic(dto.getIsPublic());
        }

        profile.updateTimestamp();
        Profile savedProfile = profileRepository.save(profile);

        redisService.invalidateByUserId(userId.toString());
        if (savedProfile.getUsername() != null) {
            redisService.invalidateProfile(savedProfile.getUsername());
        }
        log.info("Cache invalidado após atualização de perfil: userId={}", userId);

        return savedProfile;
    }

    @Transactional
    public Profile uploadAvatar(UUID userId, MultipartFile file) {
        Profile profile = findByUserId(userId);

        if (profile.getAvatarPath() != null) {
            fileStorageService.deleteFile(profile.getAvatarPath());
        }

        String filename = fileStorageService.storeFile(file);
        profile.setAvatarPath(filename);
        profile.updateTimestamp();

        Profile savedProfile = profileRepository.save(profile);

        redisService.invalidateByUserId(userId.toString());
        if (savedProfile.getUsername() != null) {
            redisService.invalidateProfile(savedProfile.getUsername());
        }
        log.info("Cache invalidado após upload de avatar: userId={}", userId);

        return savedProfile;
    }

    @Transactional
    public void deleteAvatar(UUID userId) {
        Profile profile = findByUserId(userId);

        if (profile.getAvatarPath() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Perfil não possui avatar"
            );
        }

        fileStorageService.deleteFile(profile.getAvatarPath());

        profile.setAvatarPath(null);
        profile.updateTimestamp();
        profileRepository.save(profile);

        redisService.invalidateByUserId(userId.toString());
        if (profile.getUsername() != null) {
            redisService.invalidateProfile(profile.getUsername());
        }
        log.info("Cache invalidado após remoção de avatar: userId={}", userId);
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        String username = baseUsername;
        int counter = 1;

        while (profileRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}