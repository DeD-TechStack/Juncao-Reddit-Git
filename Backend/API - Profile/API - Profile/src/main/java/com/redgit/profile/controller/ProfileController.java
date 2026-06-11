package com.redgit.profile.controller;

import com.redgit.profile.controller.dto.ProfileDTO;
import com.redgit.profile.controller.dto.PublicProfileDTO;
import com.redgit.profile.controller.dto.UpdateProfileDTO;
import com.redgit.profile.infrastructure.entities.Profile;
import com.redgit.profile.infrastructure.entities.User;
import com.redgit.profile.infrastructure.repository.UserRepository;
import com.redgit.profile.infrastructure.storage.FileStorageService;
import com.redgit.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    // ========== ENDPOINTS PRIVADOS (Autenticados) ==========

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile(
            @AuthenticationPrincipal String userEmail,
            HttpServletRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
        UUID userId = user.getId();

        Profile profile = profileService.getOrCreateProfile(userId, userEmail);
        String baseUrl = getBaseUrl(request);

        return ResponseEntity.ok(new ProfileDTO(profile, baseUrl));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileDTO> updateMyProfile(
            @AuthenticationPrincipal String userEmail,
            @RequestBody @Valid UpdateProfileDTO dto,
            HttpServletRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
        UUID userId = user.getId();

        Profile updated = profileService.updateProfile(userId, dto);
        String baseUrl = getBaseUrl(request);

        return ResponseEntity.ok(new ProfileDTO(updated, baseUrl));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<ProfileDTO> uploadAvatar(
            @AuthenticationPrincipal String userEmail,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
        UUID userId = user.getId();

        Profile updated = profileService.uploadAvatar(userId, file);
        audit.info("[AUDIT] UPLOAD_AVATAR | executadoPor={} | ts={}", userEmail, Instant.now());
        String baseUrl = getBaseUrl(request);

        return ResponseEntity.ok(new ProfileDTO(updated, baseUrl));
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<Void> deleteAvatar(
            @AuthenticationPrincipal String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
        UUID userId = user.getId();

        profileService.deleteAvatar(userId);
        audit.info("[AUDIT] DELETE_AVATAR | executadoPor={} | ts={}", userEmail, Instant.now());

        return ResponseEntity.noContent().build();
    }

    // ========== ENDPOINTS PÚBLICOS ==========

    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileDTO> getPublicProfile(
            @PathVariable String username,
            HttpServletRequest request) {

        Profile profile = profileService.findByUsername(username);

        if (!profile.isPublic()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Este perfil é privado"
            );
        }

        String baseUrl = getBaseUrl(request);
        return ResponseEntity.ok(new PublicProfileDTO(profile, baseUrl));
    }

    @GetMapping("/{username}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable String username) {
        Profile profile = profileService.findByUsername(username);

        if (profile.getAvatarPath() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Este perfil não possui avatar"
            );
        }

        Resource resource = fileStorageService.loadFileAsResource(profile.getAvatarPath());

        String contentType = "application/octet-stream";
        String filename = profile.getAvatarPath();

        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (filename.endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.endsWith(".gif")) {
            contentType = "image/gif";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + username + "-avatar\"")
                .body(resource);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }
}