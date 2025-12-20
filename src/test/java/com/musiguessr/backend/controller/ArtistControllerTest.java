package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.artist.ArtistRequestDTO;
import com.musiguessr.backend.dto.artist.ArtistResponseDTO;
import com.musiguessr.backend.security.JwtAuthFilter;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.ArtistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArtistController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService artistService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getArtists_ShouldReturnList() throws Exception {
        ArtistResponseDTO artist1 = new ArtistResponseDTO(1L, "Pink Floyd");
        ArtistResponseDTO artist2 = new ArtistResponseDTO(2L, "The Beatles");
        when(artistService.getArtists(null, null, null)).thenReturn(List.of(artist1, artist2));

        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Pink Floyd"))
                .andExpect(jsonPath("$[1].name").value("The Beatles"));
    }

    @Test
    void getArtistById_ShouldReturnArtist() throws Exception {
        ArtistResponseDTO artist = new ArtistResponseDTO(1L, "Daft Punk");
        when(artistService.getArtistById(1L)).thenReturn(artist);

        mockMvc.perform(get("/api/artists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Daft Punk"));
    }

    @Test
    void createArtist_ShouldReturnCreated() throws Exception {
        ArtistRequestDTO request = new ArtistRequestDTO();
        request.setName("New Artist");

        ArtistResponseDTO response = new ArtistResponseDTO(1L, "New Artist");

        when(artistService.createArtist(any(ArtistRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.name").value("New Artist"));
    }

    @Test
    void updateArtist_ShouldReturnOk() throws Exception {
        ArtistRequestDTO request = new ArtistRequestDTO();
        request.setName("Updated Name");

        ArtistResponseDTO response = new ArtistResponseDTO(1L, "Updated Name");

        when(artistService.updateArtist(eq(1L), any(ArtistRequestDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/artists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteArtist_ShouldReturnNoContent() throws Exception {
        doNothing().when(artistService).deleteArtist(1L);

        mockMvc.perform(delete("/api/artists/1"))
                .andExpect(status().isNoContent());
    }
}