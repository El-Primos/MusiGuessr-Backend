package com.musiguessr.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiguessr.backend.dto.post.PostShareRequestDTO;
import com.musiguessr.backend.dto.post.PostShareResponseDTO;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.PostService;
import com.musiguessr.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    private final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

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
    void shareGameHistory_ShouldReturnCreated() throws Exception {
        PostShareRequestDTO request = new PostShareRequestDTO();
        request.setGameHistoryId(10L);

        PostShareResponseDTO response = new PostShareResponseDTO();
        response.setPostId(100L);
        response.setGameScore(500);

        when(postService.shareGameHistory(eq(CURRENT_USER_ID), any(PostShareRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(100L));
    }

    @Test
    @WithMockUser
    void getPost_ShouldReturnOk() throws Exception {
        Long postId = 100L;
        PostShareResponseDTO response = new PostShareResponseDTO();
        response.setPostId(postId);

        when(postService.getPost(postId)).thenReturn(response);

        mockMvc.perform(get("/api/posts/{postId}", postId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId));
    }

    @Test
    @WithMockUser
    void getUserPosts_ShouldReturnList() throws Exception {
        Long targetUserId = 2L;
        PostShareResponseDTO response = new PostShareResponseDTO();
        response.setUserId(targetUserId);

        when(postService.getUserPosts(targetUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/posts/user/{userId}", targetUserId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(targetUserId));
    }

    @Test
    @WithMockUser
    void getMyPosts_ShouldReturnList() throws Exception {
        PostShareResponseDTO response = new PostShareResponseDTO();
        response.setUserId(CURRENT_USER_ID);

        when(postService.getUserPosts(CURRENT_USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/posts/me")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(CURRENT_USER_ID));

        verify(postService).getUserPosts(CURRENT_USER_ID);
    }

    @Test
    @WithMockUser
    void deletePost_ShouldReturnOk() throws Exception {
        Long postId = 100L;

        mockMvc.perform(delete("/api/posts/{postId}", postId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Post deleted successfully"));

        verify(postService).deletePost(CURRENT_USER_ID, postId);
    }
}