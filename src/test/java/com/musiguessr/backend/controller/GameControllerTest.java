package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.game.*;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createGame_ShouldReturnCreated() throws Exception {
        GameResponseDTO response = new GameResponseDTO(1L, "CREATED", 100L, 5L);
        when(gameService.createGame()).thenReturn(response);

        mockMvc.perform(post("/api/games")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("CREATED"));
    }

    @Test
    @WithMockUser
    void createTournamentGame_ShouldReturnCreated() throws Exception {
        Long tournamentId = 50L;
        GameResponseDTO response = new GameResponseDTO(2L, "CREATED", 100L, 5L);
        when(gameService.createTournamentGame(tournamentId)).thenReturn(response);

        mockMvc.perform(post("/api/games/tournament")
                        .param("tournamentId", String.valueOf(tournamentId))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @WithMockUser
    void startGame_ShouldReturnOk() throws Exception {
        Long gameId = 1L;
        GameStartDTO startDTO = new GameStartDTO();
        startDTO.setId(gameId);
        startDTO.setCurrentRound(1);
        startDTO.setNextPreviewUrl("http://preview");

        when(gameService.startGame(gameId)).thenReturn(startDTO);

        mockMvc.perform(post("/api/games/{id}/start", gameId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPreviewUrl").value("http://preview"));
    }

    @Test
    @WithMockUser
    void guess_ShouldReturnResult() throws Exception {
        Long gameId = 1L;
        GameRoundGuessDTO request = new GameRoundGuessDTO();
        request.setMusicId(10L);
        request.setElapsedMs(5000L);

        GameRoundResultDTO result = new GameRoundResultDTO();
        result.setCorrect(true);
        result.setEarnedScore(850);

        when(gameService.guess(eq(gameId), any(GameRoundGuessDTO.class))).thenReturn(result);

        mockMvc.perform(post("/api/games/{id}/guess", gameId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.earnedScore").value(850));
    }

    @Test
    @WithMockUser
    void skip_ShouldReturnResult() throws Exception {
        Long gameId = 1L;
        GameRoundResultDTO result = new GameRoundResultDTO();
        result.setCorrect(false);
        result.setEarnedScore(0);

        when(gameService.skip(gameId)).thenReturn(result);

        mockMvc.perform(get("/api/games/{id}/skip", gameId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));
    }

    @Test
    @WithMockUser
    void finish_ShouldReturnFinalResult() throws Exception {
        Long gameId = 1L;
        GameResultDTO result = new GameResultDTO();
        result.setId(gameId);
        result.setFinalScore(1500);

        when(gameService.finish(gameId)).thenReturn(result);

        mockMvc.perform(post("/api/games/{id}/finish", gameId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalScore").value(1500));
    }
}