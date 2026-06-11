package com.redgit.profile.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redgit.profile.infrastructure.entities.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String location;
    private String website;
    private Map<String, String> socialLinks;

    @JsonProperty("isPublic")
    private boolean isPublic;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProfileDTO(Profile profile, String baseUrl) {
        this.id = profile.getId();
        this.userId = profile.getUserId();
        this.username = profile.getUsername();
        this.displayName = profile.getDisplayName();
        this.bio = profile.getBio();
        this.avatarUrl = profile.getAvatarPath() != null
                ? baseUrl + "/api/profiles/" + profile.getUsername() + "/avatar"
                : null;
        this.location = profile.getLocation();
        this.website = profile.getWebsite();
        this.socialLinks = profile.getSocialLinks();
        this.isPublic = profile.isPublic();
        this.createdAt = profile.getCreatedAt();
        this.updatedAt = profile.getUpdatedAt();
    }
}
