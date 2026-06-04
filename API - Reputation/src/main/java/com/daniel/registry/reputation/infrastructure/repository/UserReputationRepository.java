package com.daniel.registry.reputation.infrastructure.repository;

import com.daniel.registry.reputation.infrastructure.entity.UserReputation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReputationRepository extends JpaRepository<UserReputation, Long> {
    Optional<UserReputation> findByUserEmail(String userEmail);
}
