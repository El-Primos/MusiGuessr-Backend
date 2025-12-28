package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.following.FollowRequestDTO;
import com.musiguessr.backend.model.Following;
import com.musiguessr.backend.model.FollowingId;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.FollowingRepository;
import com.musiguessr.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowingServiceTest {

    @Mock
    private FollowingRepository followingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowingService followingService;

    @Test
    void sendFollowRequest_ShouldSaveFollowing_WhenValid() {
        Long requesterId = 1L;
        Long targetId = 2L;

        when(userRepository.existsById(requesterId)).thenReturn(true);
        when(userRepository.existsById(targetId)).thenReturn(true);
        when(followingRepository.findById(any(FollowingId.class))).thenReturn(Optional.empty());

        when(userRepository.getReferenceById(requesterId)).thenReturn(new User());
        when(userRepository.getReferenceById(targetId)).thenReturn(new User());

        followingService.sendFollowRequest(requesterId, targetId);

        verify(followingRepository, times(1)).save(any(Following.class));
    }

    @Test
    void sendFollowRequest_ShouldThrowException_WhenSelfFollow() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                followingService.sendFollowRequest(1L, 1L)
        );
        verify(followingRepository, never()).save(any());
    }

    @Test
    void sendFollowRequest_ShouldThrowException_WhenAlreadySent() {
        Long requesterId = 1L;
        Long targetId = 2L;
        FollowingId id = new FollowingId(requesterId, targetId);
        Following existing = new Following();
        existing.setAccepted(false);

        when(userRepository.existsById(requesterId)).thenReturn(true);
        when(userRepository.existsById(targetId)).thenReturn(true);
        when(followingRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
                followingService.sendFollowRequest(requesterId, targetId)
        );
    }

    @Test
    void sendFollowRequest_ShouldThrowException_WhenReverseRequestExists() {
        Long requesterId = 1L;
        Long targetId = 2L;
        FollowingId id = new FollowingId(requesterId, targetId);
        FollowingId reverseId = new FollowingId(targetId, requesterId);

        Following reverseRequest = new Following();
        reverseRequest.setAccepted(false);

        when(userRepository.existsById(requesterId)).thenReturn(true);
        when(userRepository.existsById(targetId)).thenReturn(true);
        when(followingRepository.findById(id)).thenReturn(Optional.empty()); // Direct request doesn't exist
        when(followingRepository.findById(reverseId)).thenReturn(Optional.of(reverseRequest)); // But reverse does

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                followingService.sendFollowRequest(requesterId, targetId)
        );
        assertTrue(exception.getMessage().contains("please accept instead"));
    }

    @Test
    void acceptRequest_ShouldUpdateAndCreateReverse_WhenValid() {
        Long userId = 1L;
        Long requesterId = 2L;
        FollowingId id = new FollowingId(requesterId, userId);

        Following request = new Following();
        request.setId(id);
        request.setAccepted(false);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(followingRepository.findById(id)).thenReturn(Optional.of(request));

        FollowingId reverseId = new FollowingId(userId, requesterId);
        when(followingRepository.existsById(reverseId)).thenReturn(false);

        followingService.acceptRequest(userId, requesterId);

        assertTrue(request.getAccepted());
        verify(followingRepository, times(1)).save(request);
        verify(followingRepository, times(1)).save(argThat(f ->
                f.getId().equals(reverseId) && f.getAccepted()
        ));
    }

    @Test
    void acceptRequest_ShouldThrow_WhenRequestNotFound() {
        Long userId = 1L;
        Long requesterId = 2L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(followingRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                followingService.acceptRequest(userId, requesterId)
        );
    }

    @Test
    void discardRequest_ShouldDelete_WhenFound() {
        Long userId = 1L;
        Long requesterId = 2L;
        FollowingId id = new FollowingId(requesterId, userId);
        Following request = new Following();
        request.setAccepted(false);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(followingRepository.findById(id)).thenReturn(Optional.of(request));

        followingService.discardRequest(userId, requesterId);

        verify(followingRepository).deleteById(id);
    }

    @Test
    void unfriend_ShouldDeleteBothDirections_WhenFriendshipExists() {
        Long userId = 1L;
        Long friendId = 2L;

        FollowingId outId = new FollowingId(userId, friendId);
        FollowingId inId = new FollowingId(friendId, userId);

        Following outgoing = new Following();
        outgoing.setAccepted(true);
        Following incoming = new Following();
        incoming.setAccepted(true);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(friendId)).thenReturn(true);

        when(followingRepository.findById(outId)).thenReturn(Optional.of(outgoing));
        when(followingRepository.findById(inId)).thenReturn(Optional.of(incoming));

        followingService.unfriend(userId, friendId);

        verify(followingRepository).deleteById(outId);
        verify(followingRepository).deleteById(inId);
    }

    @Test
    void markInboxSeen_ShouldUpdatePendingFlag() {
        Long userId = 1L;
        Following f1 = new Following();
        f1.setPending(false);
        Following f2 = new Following();
        f2.setPending(true);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(followingRepository.findByIdFollowingId(userId)).thenReturn(List.of(f1, f2));

        followingService.markInboxSeen(userId);

        assertTrue(f1.getPending());
        verify(followingRepository).saveAll(anyList());
    }

    @Test
    void listIncomingRequests_ShouldMapDTOs() {
        Long userId = 1L;
        User requester = new User();
        requester.setUsername("requesterUser");
        Following f = new Following();
        f.setId(new FollowingId(2L, userId));
        f.setUser(requester);
        f.setPending(false);
        f.setAccepted(false);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(followingRepository.findByIdFollowingIdAndAcceptedFalse(userId)).thenReturn(List.of(f));

        List<FollowRequestDTO> result = followingService.listIncomingRequests(userId);

        assertEquals(1, result.size());
        assertEquals("requesterUser", result.getFirst().getRequesterUsername());
    }
}