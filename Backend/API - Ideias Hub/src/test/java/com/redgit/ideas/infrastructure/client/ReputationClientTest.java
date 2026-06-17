package com.redgit.ideas.infrastructure.client;

import com.redgit.ideas.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@DisplayName("ReputationClient Tests")
class ReputationClientTest {

    private static final String TEST_SECRET = "3246918694727278232479912314703835454208642542872406260685881546";
    private static final String SERVICE_TOKEN = "test-internal-service-secret-for-context-load";

    private MockRestServiceServer mockServer;
    private ReputationClient reputationClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        TokenService tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", TEST_SECRET);

        reputationClient = new ReputationClient(builder, "http://localhost:8084", SERVICE_TOKEN, tokenService);
    }

    @Test
    @DisplayName("Deve chamar o endpoint de eventos do Reputation no caminho feliz")
    void notifyLikeGained_happyPath_shouldCallReputationEndpoint() {
        // Arrange
        mockServer.expect(requestTo("http://localhost:8084/reputation/events"))
                .andExpect(method(POST))
                .andExpect(header("X-Service-Token", SERVICE_TOKEN))
                .andExpect(header("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        // Act & Assert (não deve lançar exceção)
        reputationClient.notifyLikeGained("author@test.com", "test-id-123");

        mockServer.verify();
    }

    @Test
    @DisplayName("Nao deve lancar exceção quando o Reputation responde com erro")
    void notifyLikeGained_whenReputationFails_shouldNotThrow() {
        // Arrange
        mockServer.expect(requestTo("http://localhost:8084/reputation/events"))
                .andExpect(method(POST))
                .andRespond(withServerError());

        // Act & Assert
        reputationClient.notifyLikeGained("author@test.com", "test-id-123");

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve chamar o endpoint de eventos com contributorId ao aceitar contribuição")
    void notifyContributionAccepted_happyPath_shouldCallReputationEndpoint() {
        mockServer.expect(requestTo("http://localhost:8084/reputation/events"))
                .andExpect(method(POST))
                .andExpect(header("X-Service-Token", SERVICE_TOKEN))
                .andExpect(header("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        reputationClient.notifyContributionAccepted("contributor@test.com", "contrib-id-1");

        mockServer.verify();
    }

    @Test
    @DisplayName("Nao deve lancar exceção quando Reputation falha ao aceitar contribuição")
    void notifyContributionAccepted_whenReputationFails_shouldNotThrow() {
        mockServer.expect(requestTo("http://localhost:8084/reputation/events"))
                .andExpect(method(POST))
                .andRespond(withServerError());

        reputationClient.notifyContributionAccepted("contributor@test.com", "contrib-id-1");

        mockServer.verify();
    }
}
