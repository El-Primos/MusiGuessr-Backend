package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.leaderboard.LeaderboardEntryDTO;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.LeaderboardService;
import com.musiguessr.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaderboardController.class)
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @MockitoBean
    private AuthUtil authUtil;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final Long CURRENT_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        when(authUtil.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
    }

    @Test
    @WithMockUser
    void getGlobalLeaderboard_ShouldReturnList() throws Exception {
        LeaderboardEntryDTO entry = new LeaderboardEntryDTO(1, 10L, "pro_gamer", 1000);
        when(leaderboardService.getGlobalLeaderboard()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboards/global")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("pro_gamer"))
                .andExpect(jsonPath("$[0].score").value(1000));

        verify(leaderboardService).getGlobalLeaderboard();
    }

    @Test
    @WithMockUser
    void getPlaylistLeaderboard_ShouldReturnList() throws Exception {
        Long playlistId = 5L;
        LeaderboardEntryDTO entry = new LeaderboardEntryDTO(1, 10L, "music_fan", 500);
        when(leaderboardService.getPlaylistLeaderboard(playlistId)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboards/playlist/{id}", playlistId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("music_fan"));

        verify(leaderboardService).getPlaylistLeaderboard(playlistId);
    }

    @Test
    @WithMockUser
    void getTournamentLeaderboard_ShouldReturnList() throws Exception {
        Long tournamentId = 20L;
        LeaderboardEntryDTO entry = new LeaderboardEntryDTO(1, 15L, "tourney_winner", 2000);
        when(leaderboardService.getTournamentLeaderboard(tournamentId)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboards/tournament/{id}", tournamentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1));

        verify(leaderboardService).getTournamentLeaderboard(tournamentId);
    }

    @Test
    @WithMockUser
    void getFriendsLeaderboard_ShouldReturnList() throws Exception {
        LeaderboardEntryDTO entry = new LeaderboardEntryDTO(1, CURRENT_USER_ID, "me", 100);
        when(leaderboardService.getFriendsLeaderboard(CURRENT_USER_ID)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboards/friends")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(CURRENT_USER_ID));

        verify(leaderboardService).getFriendsLeaderboard(CURRENT_USER_ID);
    }
}