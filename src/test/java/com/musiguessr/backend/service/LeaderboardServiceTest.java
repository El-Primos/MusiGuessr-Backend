package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.leaderboard.LeaderboardEntryDTO;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private TournamentParticipantRepository tournamentParticipantRepository;
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FollowingRepository followingRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    void getGlobalLeaderboard_ShouldReturnEntries() {
        Object[] row1 = {1L, 100};
        Object[] row2 = {2L, 80};
        List<Object[]> repoResults = List.of(row1, row2);

        when(userRepository.findGlobalLeaderboard(any(Pageable.class))).thenReturn(repoResults);

        User u1 = new User();
        u1.setUsername("user1");
        User u2 = new User();
        u2.setUsername("user2");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u2));

        List<LeaderboardEntryDTO> result = leaderboardService.getGlobalLeaderboard();

        assertEquals(2, result.size());
        assertEquals("user1", result.getFirst().getUsername());
        assertEquals(1, result.getFirst().getRank());
        assertEquals(100, result.getFirst().getScore());
    }

    @Test
    void getPlaylistLeaderboard_ShouldThrow_WhenPlaylistNotFound() {
        Long playlistId = 99L;
        when(playlistRepository.existsById(playlistId)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                leaderboardService.getPlaylistLeaderboard(playlistId)
        );
    }

    @Test
    void getPlaylistLeaderboard_ShouldReturnEntries_WhenFound() {
        Long playlistId = 1L;
        Object[] row = {1L, 50};

        when(playlistRepository.existsById(playlistId)).thenReturn(true);
        when(userRepository.findGlobalLeaderboard(any(Pageable.class))).thenReturn(List.<Object[]>of(row));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        List<LeaderboardEntryDTO> result = leaderboardService.getPlaylistLeaderboard(playlistId);

        assertFalse(result.isEmpty());
    }

    @Test
    void getTournamentLeaderboard_ShouldRankParticipants() {
        Long tournamentId = 10L;

        User user1 = new User();
        user1.setUsername("winner");
        User user2 = new User();
        user2.setUsername("runnerup");

        TournamentParticipant p1 = new TournamentParticipant();
        p1.setId(new TournamentParticipantId(tournamentId, 1L));
        p1.setUser(user1);
        p1.setUserScore(200);

        TournamentParticipant p2 = new TournamentParticipant();
        p2.setId(new TournamentParticipantId(tournamentId, 2L));
        p2.setUser(user2);
        p2.setUserScore(150);

        List<TournamentParticipant> participants = List.of(p1, p2);

        when(tournamentRepository.existsById(tournamentId)).thenReturn(true);
        when(tournamentParticipantRepository.findByIdTournamentIdOrderByUserScoreDesc(tournamentId))
                .thenReturn(participants);

        List<LeaderboardEntryDTO> result = leaderboardService.getTournamentLeaderboard(tournamentId);

        assertEquals(2, result.size());

        assertEquals(1, result.getFirst().getRank());
        assertEquals("winner", result.get(0).getUsername());
        assertEquals(200, result.get(0).getScore());

        assertEquals(2, result.get(1).getRank());
        assertEquals("runnerup", result.get(1).getUsername());
    }

    @Test
    void getFriendsLeaderboard_ShouldFilterMutualFriends() {
        Long currentUserId = 1L;
        Long mutualFriendId = 2L;
        Long oneWayFollowingId = 3L;
        Long oneWayFollowerId = 4L;

        when(userRepository.existsById(currentUserId)).thenReturn(true);

        Following out1 = new Following();
        out1.setId(new FollowingId(currentUserId, mutualFriendId));
        Following out2 = new Following();
        out2.setId(new FollowingId(currentUserId, oneWayFollowingId));
        when(followingRepository.findByIdUserIdAndAcceptedTrue(currentUserId))
                .thenReturn(List.of(out1, out2));

        Following in1 = new Following();
        in1.setId(new FollowingId(mutualFriendId, currentUserId));
        Following in2 = new Following();
        in2.setId(new FollowingId(oneWayFollowerId, currentUserId));
        when(followingRepository.findByIdFollowingIdAndAcceptedTrue(currentUserId))
                .thenReturn(List.of(in1, in2));


        Object[] row1 = {1L, 500};
        Object[] row2 = {2L, 300};

        when(userRepository.findFriendsLeaderboard(anyList(), any(Pageable.class)))
                .thenReturn(List.of(row1, row2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));

        List<LeaderboardEntryDTO> result = leaderboardService.getFriendsLeaderboard(currentUserId);

        assertEquals(2, result.size());
        verify(userRepository).findFriendsLeaderboard(
                argThat(list -> list.contains(mutualFriendId) && list.contains(currentUserId) && !list.contains(oneWayFollowingId)),
                any(Pageable.class)
        );
    }
}