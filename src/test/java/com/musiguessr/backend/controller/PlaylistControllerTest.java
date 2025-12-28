package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.playlist.*;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(PlaylistController.class)
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getPlaylists_ShouldReturnList() throws Exception {
        PlaylistResponseDTO dto = new PlaylistResponseDTO(1L, "MyList", 1L);
        when(playlistService.getPlaylists(null, null, null, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/playlists")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MyList"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPlaylist_ShouldReturnCreated() throws Exception {
        PlaylistRequestDTO request = new PlaylistRequestDTO();
        request.setName("New PL");
        request.setOwnerId(1L);

        PlaylistResponseDTO response = new PlaylistResponseDTO("Created", 1L, "New PL", 1L);
        when(playlistService.createPlaylist(any(PlaylistRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/playlists")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRandomPlaylist_ShouldReturnCreated() throws Exception {
        PlaylistRandomRequestDTO request = new PlaylistRandomRequestDTO();
        request.setName("Random");
        request.setLength(5);

        when(playlistService.createRandomPlaylist(any(PlaylistRandomRequestDTO.class)))
                .thenReturn(new PlaylistResponseDTO(1L, "Random", 1L));

        mockMvc.perform(post("/api/playlists/random")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePlaylist_ShouldReturnNoContent() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/api/playlists/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(playlistService).deletePlaylist(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addSongToPlaylist_ShouldReturnCreated() throws Exception {
        Long id = 1L;
        PlaylistItemRequestDTO request = new PlaylistItemRequestDTO();
        request.setSongId(10L);
        request.setPosition(1);

        mockMvc.perform(post("/api/playlists/{id}/songs", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(playlistService).addSongToPlaylist(eq(id), any(PlaylistItemRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reorder_ShouldReturnOk() throws Exception {
        Long id = 1L;
        PlaylistBatchItemRequestDTO request = new PlaylistBatchItemRequestDTO();
        PlaylistItemRequestDTO item = new PlaylistItemRequestDTO();
        item.setSongId(10L);
        item.setPosition(1);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/playlists/{id}/reorder", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(playlistService).reorder(eq(id), any(PlaylistBatchItemRequestDTO.class));
    }
}