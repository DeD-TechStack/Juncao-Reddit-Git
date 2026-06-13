package com.redgit.profile.controller.dto;

import com.redgit.profile.infrastructure.entities.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicProfileDTO {
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String location;
    private String website;
    private Map<String, String> socialLinks;
    private LocalDateTime createdAt;

    public PublicProfileDTO(Profile profile, String baseUrl) {
        this.username = profile.getUsername();
        this.displayName = profile.getDisplayName();
        this.bio = profile.getBio();
        this.avatarUrl = profile.getAvatarPath() != null
                ? baseUrl + "/api/profiles/" + profile.getUsername() + "/avatar"
                : null;
        this.location = profile.getLocation();
        this.website = profile.getWebsite();
        this.socialLinks = profile.getSocialLinks();
        this.createdAt = profile.getCreatedAt();
    }
}