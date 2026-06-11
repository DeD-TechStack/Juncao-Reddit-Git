package com.daniel.registry.trending.dto;

import java.util.List;

public record TrendingResponseDTO(
        String period,
        String key,
        List<TrendingItemDTO> items
) {}
