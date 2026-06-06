package com.daniel.registry.trending.controller;

import com.daniel.registry.trending.dto.TrendingItemDTO;
import com.daniel.registry.trending.service.TrendingService;
import com.daniel.registry.trending.util.TrendingKeys;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trending")
public class TrendingController {

    private final TrendingService trendingService;

    public TrendingController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }

    @PostMapping("/ideas/{ideaId}/like")
    public ResponseEntity<Void> like(@PathVariable long ideaId) {
        trendingService.bumpLike(ideaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ideas/{ideaId}/score")
    public ResponseEntity<Void> score(@PathVariable long ideaId, @RequestParam double delta) {
        trendingService.bumpScore(ideaId, delta);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/daily")
    public List<TrendingItemDTO> daily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(defaultValue = "10") int limit
    ) {
        LocalDate target = (date != null) ? date : LocalDate.now(TrendingKeys.zone());
        return trendingService.topDaily(target, limit);
    }

    @GetMapping("/weekly")
    public List<TrendingItemDTO> weekly(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(defaultValue = "10") int limit
    ) {
        LocalDate target = (date != null) ? date : LocalDate.now(TrendingKeys.zone());
        return trendingService.topWeekly(target, limit);
    }
}
