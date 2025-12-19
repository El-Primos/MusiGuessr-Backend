package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.genre.GenreRequestDTO;
import com.musiguessr.backend.dto.genre.GenreResponseDTO;
import com.musiguessr.backend.service.GenreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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

@WebMvcTest(GenreController.class)
@AutoConfigureMockMvc(addFilters = false)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllGenres_ShouldReturnList() throws Exception {
        GenreResponseDTO genre1 = new GenreResponseDTO(1L, "Rock");
        GenreResponseDTO genre2 = new GenreResponseDTO(1L, "Pop");
        when(genreService.getAllGenres()).thenReturn(List.of(genre1, genre2));

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rock"))
                .andExpect(jsonPath("$[1].name").value("Pop"));
    }

    @Test
    void getGenreById_ShouldReturnGenre() throws Exception {
        GenreResponseDTO genre = new GenreResponseDTO(1L, "Jazz");
        when(genreService.getGenreById(1L)).thenReturn(genre);

        mockMvc.perform(get("/api/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jazz"));
    }

    @Test
    void createGenre_ShouldReturnCreated() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO();
        request.setName("Pop");

        GenreResponseDTO response = new GenreResponseDTO(1L, "Pop");
        when(genreService.createGenre(any(GenreRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pop"));
    }

    @Test
    void updateGenre_ShouldReturnOk() throws Exception {
        GenreRequestDTO request = new GenreRequestDTO();
        request.setName("Updated Pop");

        GenreResponseDTO response = new GenreResponseDTO(1L, "Updated Pop");
        when(genreService.updateGenre(eq(1L), any(GenreRequestDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/genres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pop"));
    }

    @Test
    void deleteGenre_ShouldReturnNoContent() throws Exception {
        doNothing().when(genreService).deleteGenre(1L);

        mockMvc.perform(delete("/api/genres/1"))
                .andExpect(status().isNoContent());
    }
}