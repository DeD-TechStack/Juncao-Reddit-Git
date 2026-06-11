package com.daniel.registry.reputation.controller;

import com.daniel.registry.reputation.dto.ReputationEventResponseDTO;
import com.daniel.registry.reputation.dto.ReputationResponseDTO;
import com.daniel.registry.reputation.infrastructure.entity.ReputationEventType;
import com.daniel.registry.reputation.security.TokenService;
import com.daniel.registry.reputation.service.ReputationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReputationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ReputationControllerTest.TestBeans.class)
class ReputationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ReputationService reputationService;
    @Autowired TokenService tokenService;

    @TestConfiguration
    static class TestBeans {
        @Bean
        ReputationService reputationService() {
            return Mockito.mock(ReputationService.class);
        }

        @Bean
        TokenService tokenService() {
            return Mockito.mock(TokenService.class);
        }
    }

    private UsernamePasswordAuthenticationToken principalEmail(String email) {
        return new UsernamePasswordAuthenticationToken(email, "N/A");
    }

    @BeforeEach
    void setup() {
        Mockito.reset(reputationService, tokenService);
    }

    @Test
    void me_deveRetornarReputationDoUsuario() throws Exception {
        when(reputationService.me("daniel@email.com"))
                .thenReturn(new ReputationResponseDTO("daniel@email.com", 120, 2, "Criador Promissor"));

        mvc.perform(get("/reputation/me")
                        .principal(principalEmail("daniel@email.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("daniel@email.com"))
                .andExpect(jsonPath("$.xp").value(120))
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.title").value("Criador Promissor"));
    }

    @Test
    void event_deveAplicarEvento() throws Exception {
        when(reputationService.applyEvent("daniel@email.com", ReputationEventType.LIKE_GAINED))
                .thenReturn(new ReputationEventResponseDTO("LIKE_GAINED", 10, 10, 1, "Inovador Iniciante"));

        mvc.perform(post("/reputation/events/LIKE_GAINED")
                        .principal(principalEmail("daniel@email.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("LIKE_GAINED"))
                .andExpect(jsonPath("$.gainedXp").value(10))
                .andExpect(jsonPath("$.totalXp").value(10));
    }
}
