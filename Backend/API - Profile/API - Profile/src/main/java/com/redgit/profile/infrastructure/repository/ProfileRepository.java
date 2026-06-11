package com.redgit.profile.infrastructure.repository;

import com.redgit.profile.infrastructure.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    Optional<Profile> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUserId(UUID userId);
}
