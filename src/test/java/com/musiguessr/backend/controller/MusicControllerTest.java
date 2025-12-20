package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.music.*;
import com.musiguessr.backend.service.MusicService;
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

@WebMvcTest(MusicController.class)
@AutoConfigureMockMvc(addFilters = false)
class MusicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MusicService musicService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUploadUrl_ShouldReturnPresignedUrl() throws Exception {
        PresignRequestDTO request = new PresignRequestDTO();
        request.setName("Song");
        request.setFileName("song.mp3");
        request.setContent_type("audio/mpeg");

        PresignResponseDTO response = new PresignResponseDTO("OK", "key/123", "http://s3-url");

        when(musicService.presign(any(PresignRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/musics/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upload_url").value("http://s3-url"));
    }

    @Test
    void confirmUpload_ShouldReturnCreated() throws Exception {
        // Arrange
        UploadConfirmRequestDTO request = new UploadConfirmRequestDTO();
        request.setKey("key/123");
        request.setName("Song");
        request.setGenre_id(1L);
        request.setArtist_id(2L);

        UploadConfirmResponseDTO response = new UploadConfirmResponseDTO("Uploaded", 1L, "Song", "http://url");

        when(musicService.confirm(any(UploadConfirmRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/musics/upload-confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("http://url"));
    }

    @Test
    void getAllMusic_ShouldReturnList() throws Exception {
        MusicResponseDTO music1 = new MusicResponseDTO(1L, "Song1", "url1", null, null);
        MusicResponseDTO music2 = new MusicResponseDTO(1L, "Song2", "url2", null, null);
        when(musicService.getAllMusic()).thenReturn(List.of(music1, music2));

        mockMvc.perform(get("/api/musics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Song1"))
                .andExpect(jsonPath("$[1].name").value("Song2"));
    }

    @Test
    void updateMusic_ShouldReturnOk() throws Exception {
        MusicRequestDTO request = new MusicRequestDTO();
        request.setName("New Name");

        MusicResponseDTO response = new MusicResponseDTO(1L, "New Name", "url", null, null);

        when(musicService.updateMusic(eq(1L), any(MusicRequestDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/musics/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void deleteMusic_ShouldReturnNoContent() throws Exception {
        doNothing().when(musicService).deleteMusic(1L);

        mockMvc.perform(delete("/api/musics/1"))
                .andExpect(status().isNoContent());
    }
}