package com.redgit.auth.repository;

import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve retornar um Optional<User> quando o email for encontrado")
    void findByEmailSucess() {
        User newUser = new User();
        newUser.setEmail("teste@email.com");
        newUser.setPassword("123");
        newUser.setName("Teste");
        entityManager.persist(newUser);

        Optional<User> result = userRepository.findByEmail("teste@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("teste@email.com");
        assertThat(result.get().getName()).isEqualTo("Teste");
    }

    @Test
    @DisplayName("Deve retornar um Optional vazio quando o email não for encontrado")
    void findByEmailFailure() {
        Optional<User> result = userRepository.findByEmail("naoexiste@email.com");

        assertThat(result).isEmpty();
    }
}
