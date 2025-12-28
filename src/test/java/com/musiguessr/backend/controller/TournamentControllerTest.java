package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.tournament.*;
import com.musiguessr.backend.model.TournamentState;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.TournamentService;
import com.musiguessr.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TournamentController.class)
class TournamentControllerTest {

    private final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TournamentService tournamentService;

    @MockitoBean
    private AuthUtil authUtil;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(authUtil.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
    }

    @Test
    @WithMockUser
    void getTournaments_ShouldReturnPage() throws Exception {
        TournamentResponseDTO dto = new TournamentResponseDTO();
        dto.setId(1L);
        dto.setName("Test Tourney");
        Page<TournamentResponseDTO> page = new PageImpl<>(List.of(dto));

        when(tournamentService.getTournaments(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/tournaments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Tourney"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTournament_ShouldReturnCreated() throws Exception {
        TournamentCreateRequestDTO request = new TournamentCreateRequestDTO();
        request.setName("New Tourney");
        request.setPlaylistId(100L);

        TournamentResponseDTO response = new TournamentResponseDTO();
        response.setId(10L);
        response.setName("New Tourney");

        when(tournamentService.createTournament(eq(CURRENT_USER_ID), any(TournamentCreateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/tournaments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTournamentState_ShouldReturnOk() throws Exception {
        Long id = 10L;
        TournamentStateUpdateRequestDTO request = new TournamentStateUpdateRequestDTO();
        request.setState(TournamentState.ACTIVE);

        TournamentResponseDTO response = new TournamentResponseDTO();
        response.setId(id);
        response.setStatus(TournamentState.ACTIVE);

        when(tournamentService.updateTournamentState(eq(id), eq(CURRENT_USER_ID), eq(TournamentState.ACTIVE)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/tournaments/{id}/state", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void joinTournament_ShouldReturnOk() throws Exception {
        Long id = 10L;
        when(tournamentService.joinTournament(CURRENT_USER_ID, id)).thenReturn(new TournamentResponseDTO());

        mockMvc.perform(post("/api/tournaments/{id}/join", id)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(tournamentService).joinTournament(CURRENT_USER_ID, id);
    }

    @Test
    @WithMockUser
    void getParticipants_ShouldReturnList() throws Exception {
        Long id = 10L;
        TournamentParticipantDTO p = new TournamentParticipantDTO(1L, "Player1", 100);
        when(tournamentService.getParticipants(id)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/tournaments/{id}/participants", id)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Player1"));
    }

    @Test
    @WithMockUser
    void getLeaderboard_ShouldReturnList() throws Exception {
        Long id = 10L;
        TournamentLeaderboardEntryDTO entry = new TournamentLeaderboardEntryDTO(1, 1L, "Winner", 500);
        when(tournamentService.getLeaderboard(id)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/tournaments/{id}/leaderboard", id)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1));
    }
}