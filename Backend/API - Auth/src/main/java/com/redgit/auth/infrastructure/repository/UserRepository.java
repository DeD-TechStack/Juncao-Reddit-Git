package com.redgit.auth.infrastructure.repository;

import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);
    List<User> findByEnabled(boolean enabled);
    List<User> findByAccountNonLocked(boolean locked);
}