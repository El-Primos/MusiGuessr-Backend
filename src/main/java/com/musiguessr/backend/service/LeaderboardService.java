package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.leaderboard.LeaderboardEntryDTO;
import com.musiguessr.backend.model.TournamentParticipant;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.PlaylistRepository;
import com.musiguessr.backend.repository.TournamentParticipantRepository;
import com.musiguessr.backend.repository.TournamentRepository;
import com.musiguessr.backend.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final TournamentRepository tournamentRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final com.musiguessr.backend.repository.FollowingRepository followingRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getGlobalLeaderboard() {
        Pageable pageable = PageRequest.of(0, 100);
        List<Object[]> results = userRepository.findGlobalLeaderboard(pageable);
        return mapToLeaderboard(results);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getPlaylistLeaderboard(Long playlistId) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
        }

        Pageable pageable = PageRequest.of(0, 100);
        List<Object[]> results = userRepository.findGlobalLeaderboard(pageable);

        return mapToLeaderboard(results);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getTournamentLeaderboard(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found");
        }

        List<TournamentParticipant> participants =
                tournamentParticipantRepository.findByIdTournamentIdOrderByUserScoreDesc(tournamentId);

        return IntStream.range(0, participants.size())
                .mapToObj(i -> {
                    TournamentParticipant p = participants.get(i);
                    User user = p.getUser();
                    return new LeaderboardEntryDTO(
                            i + 1,
                            p.getId().getUserId(),
                            user != null ? user.getUsername() : null,
                            p.getUserScore()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getFriendsLeaderboard(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Get mutual friends: users who follow current user AND current user follows them (both accepted)
        List<Long> friendIds = getMutualFriendIds(userId);

        // Include the current user in the leaderboard
        friendIds.add(userId);

        // If no friends (only current user), still show them
        if (friendIds.isEmpty()) {
            friendIds.add(userId);
        }

        Pageable pageable = PageRequest.of(0, 100);
        List<Object[]> results = userRepository.findFriendsLeaderboard(friendIds, pageable);

        return mapToLeaderboard(results);
    }

    private List<Long> getMutualFriendIds(Long userId) {
        // Get users that current user follows (and accepted)
        List<Long> following = followingRepository.findByIdUserIdAndAcceptedTrue(userId)
                .stream()
                .map(f -> f.getId().getFollowingId())
                .collect(Collectors.toList());

        // Get users that follow current user (and accepted)
        List<Long> followers = followingRepository.findByIdFollowingIdAndAcceptedTrue(userId)
                .stream()
                .map(f -> f.getId().getUserId())
                .collect(Collectors.toList());

        // Find mutual friends (intersection of following and followers)
        return following.stream()
                .filter(followers::contains)
                .collect(Collectors.toList());
    }

    // ---------------- helper ----------------

    private List<LeaderboardEntryDTO> mapToLeaderboard(List<Object[]> results) {
        return IntStream.range(0, results.size())
                .mapToObj(i -> {
                    Object[] row = results.get(i);
                    Long userId = (Long) row[0];
                    Integer score = ((Number) row[1]).intValue();

                    String username = userRepository.findById(userId)
                            .map(User::getUsername)
                            .orElse(null);

                    return new LeaderboardEntryDTO(i + 1, userId, username, score);
                })
                .collect(Collectors.toList());
    }
}
