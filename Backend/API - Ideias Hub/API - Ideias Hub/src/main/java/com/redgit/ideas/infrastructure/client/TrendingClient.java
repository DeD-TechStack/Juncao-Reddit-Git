package com.redgit.ideas.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class TrendingClient {

    private final RestClient restClient;

    public TrendingClient(RestClient.Builder restClientBuilder,
                           @Value("${trending.api.url}") String trendingApiUrl) {
        this.restClient = restClientBuilder.baseUrl(trendingApiUrl).build();
    }

    // Nota: a API Trending identifica ideias por um ID numerico (long), enquanto a
    // Ideias Hub usa UUID (String). Ate que essa diferenca seja resolvida, derivamos
    // um ID numerico estavel a partir do hash do UUID para notificar o like.
    public void notifyLike(String ideaId) {
        try {
            long numericId = Math.abs((long) ideaId.hashCode());
            restClient.post()
                    .uri("/trending/ideas/{ideaId}/like", numericId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Falha ao notificar like ao servico Trending para ideaId={}", ideaId, e);
        }
    }
}
