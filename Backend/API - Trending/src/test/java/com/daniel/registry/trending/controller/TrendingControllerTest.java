package com.daniel.registry.trending.controller;

import com.daniel.registry.trending.dto.TrendingItemDTO;
import com.daniel.registry.trending.dto.TrendingResponseDTO;
import com.daniel.registry.trending.security.TokenService;
import com.daniel.registry.trending.service.TrendingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrendingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrendingControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TrendingService trendingService;

    @MockitoBean
    TokenService tokenService;

    @Test
    void post_like_deveRetornar204_eChamarService() throws Exception {
        mvc.perform(post("/trending/ideas/{ideaId}/like", 10L))
                .andExpect(status().isNoContent());

        verify(trendingService).bumpLike(10L);
        verifyNoMoreInteractions(trendingService);
    }

    @Test
    void post_score_deveRetornar204_eChamarService() throws Exception {
        mvc.perform(post("/trending/ideas/{ideaId}/score", 99L)
                        .queryParam("delta", "2.5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(trendingService).bumpScore(99L, 2.5);
        verifyNoMoreInteractions(trendingService);
    }

    @Test
    void get_daily_semDate_deveRetornarLista() throws Exception {
        given(trendingService.topDaily(any(LocalDate.class), eq(10)))
                .willReturn(new TrendingResponseDTO("daily", "chave-teste", List.of(
                        new TrendingItemDTO(1L, 10.0),
                        new TrendingItemDTO(2L, 7.5)
                )));

        mvc.perform(get("/trending/daily"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].ideaId").value(1))
                .andExpect(jsonPath("$.items[0].score").value(10.0))
                .andExpect(jsonPath("$.items[1].ideaId").value(2))
                .andExpect(jsonPath("$.items[1].score").value(7.5));

        verify(trendingService).topDaily(any(LocalDate.class), eq(10));
        verifyNoMoreInteractions(trendingService);
    }

    @Test
    void get_weekly_semDate_deveRetornarLista() throws Exception {
        given(trendingService.topWeekly(any(LocalDate.class), eq(10)))
                .willReturn(new TrendingResponseDTO("weekly", "chave-teste", List.of(
                        new TrendingItemDTO(5L, 20.0)
                )));

        mvc.perform(get("/trending/weekly"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].ideaId").value(5))
                .andExpect(jsonPath("$.items[0].score").value(20.0));

        verify(trendingService).topWeekly(any(LocalDate.class), eq(10));
        verifyNoMoreInteractions(trendingService);
    }
}
