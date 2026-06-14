package com.redgit.ideas.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpMethod.POST;

@DisplayName("TrendingClient Tests")
class TrendingClientTest {

    private MockRestServiceServer mockServer;
    private TrendingClient trendingClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        trendingClient = new TrendingClient(builder, "http://localhost:8085");
    }

    @Test
    @DisplayName("Deve chamar o endpoint de like do Trending no caminho feliz")
    void notifyLike_happyPath_shouldCallTrendingEndpoint() {
        // Arrange
        mockServer.expect(requestTo(matchesPattern("http://localhost:8085/trending/ideas/\\d+/like")))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        // Act & Assert (não deve lançar exceção)
        trendingClient.notifyLike("test-id-123");

        mockServer.verify();
    }

    @Test
    @DisplayName("Nao deve lancar exceção quando o Trending responde com erro")
    void notifyLike_whenTrendingFails_shouldNotThrow() {
        // Arrange
        mockServer.expect(requestTo(matchesPattern("http://localhost:8085/trending/ideas/\\d+/like")))
                .andExpect(method(POST))
                .andRespond(withServerError());

        // Act & Assert
        trendingClient.notifyLike("test-id-123");

        mockServer.verify();
    }
}
