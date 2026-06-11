package com.daniel.registry.trending.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ServiceTokenFilter extends OncePerRequestFilter {

    @Value("${internal.service.secret}")
    private String serviceSecret;

    @PostConstruct
    private void validateSecret() {
        if (serviceSecret == null || serviceSecret.isBlank() || serviceSecret.length() < 32)
            throw new IllegalStateException("INTERNAL_SERVICE_SECRET não configurado ou inválido. Defina a variável com no mínimo 32 caracteres.");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        boolean isBumpEndpoint = "POST".equalsIgnoreCase(method)
                && (uri.matches("/trending/ideas/\\d+/like")
                    || uri.matches("/trending/ideas/\\d+/score"));

        if (isBumpEndpoint) {
            String token = request.getHeader("X-Service-Token");
            if (token == null || !serviceSecret.equals(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Service token inválido ou ausente");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
