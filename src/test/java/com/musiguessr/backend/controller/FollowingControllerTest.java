package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.following.FollowFriendDTO;
import com.musiguessr.backend.dto.following.FollowRequestDTO;
import com.musiguessr.backend.security.JwtUtil;
import com.musiguessr.backend.service.FollowingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FollowingController.class)
class FollowingControllerTest {

    private final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowingService followingService;

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
    void sendFollowRequest_ShouldReturnCreated() throws Exception {
        Long targetUserId = 2L;

        mockMvc.perform(post("/api/followings/request")
                        .param("targetUserId", String.valueOf(targetUserId))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().string("Follow request sent"));

        verify(followingService).sendFollowRequest(CURRENT_USER_ID, targetUserId);
    }

    @Test
    @WithMockUser
    void markInboxSeen_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/followings/inbox/seen")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Inbox marked as seen"));

        verify(followingService).markInboxSeen(CURRENT_USER_ID);
    }

    @Test
    @WithMockUser
    void acceptFollowRequest_ShouldReturnOk() throws Exception {
        Long requesterId = 5L;

        mockMvc.perform(post("/api/followings/accept")
                        .param("requesterId", String.valueOf(requesterId))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Request accepted"));

        verify(followingService).acceptRequest(CURRENT_USER_ID, requesterId);
    }

    @Test
    @WithMockUser
    void discardFollowRequest_ShouldReturnOk() throws Exception {
        Long requesterId = 5L;

        mockMvc.perform(delete("/api/followings/discard")
                        .param("requesterId", String.valueOf(requesterId))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Request discarded"));

        verify(followingService).discardRequest(CURRENT_USER_ID, requesterId);
    }

    @Test
    @WithMockUser
    void unfriend_ShouldReturnOk() throws Exception {
        Long friendId = 10L;

        mockMvc.perform(delete("/api/followings/unfriend")
                        .param("friendId", String.valueOf(friendId))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Friendship removed"));

        verify(followingService).unfriend(CURRENT_USER_ID, friendId);
    }

    @Test
    @WithMockUser
    void incomingRequests_ShouldReturnList() throws Exception {
        FollowRequestDTO dto = new FollowRequestDTO(2L, "user2", false, false);
        when(followingService.listIncomingRequests(CURRENT_USER_ID)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/followings/incoming")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requesterUsername").value("user2"));

        verify(followingService).listIncomingRequests(CURRENT_USER_ID);
    }

    @Test
    @WithMockUser
    void acceptedFollowing_ShouldReturnList() throws Exception {
        FollowFriendDTO dto = new FollowFriendDTO(3L, "friendUser");
        when(followingService.listAcceptedFollowing(CURRENT_USER_ID)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/followings/friends")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("friendUser"));

        verify(followingService).listAcceptedFollowing(CURRENT_USER_ID);
    }
}