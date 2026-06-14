package com.redgit.ideas.infrastructure.client;

import com.redgit.ideas.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ReputationClient {

    private final RestClient restClient;
    private final TokenService tokenService;
    private final String serviceToken;

    public ReputationClient(RestClient.Builder restClientBuilder,
                             @Value("${reputation.api.url}") String reputationApiUrl,
                             @Value("${internal.service.secret}") String serviceToken,
                             TokenService tokenService) {
        this.restClient = restClientBuilder.baseUrl(reputationApiUrl).build();
        this.tokenService = tokenService;
        this.serviceToken = serviceToken;
    }

    // Notifica o servico Reputation de que o autor da ideia ganhou reputacao por
    // receber um like. A chamada e feita em nome do autor (authorId), entao
    // geramos um token de servico de curta duracao com esse identificador.
    public void notifyLikeGained(String authorId, String ideaId) {
        try {
            String serviceJwt = tokenService.generateServiceToken(authorId);
            restClient.post()
                    .uri("/reputation/events")
                    .header("Authorization", "Bearer " + serviceJwt)
                    .header("X-Service-Token", serviceToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReputationEventRequest("LIKE_GAINED", ideaId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Falha ao notificar evento de reputacao para authorId={}", authorId, e);
        }
    }

    private record ReputationEventRequest(String type, String referenceId) {}
}
