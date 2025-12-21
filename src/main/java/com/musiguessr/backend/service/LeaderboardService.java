package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.leaderboard.LeaderboardEntryDTO;
import com.musiguessr.backend.model.TournamentParticipant;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.GameHistoryRepository;
import com.musiguessr.backend.repository.PlaylistRepository;
import com.musiguessr.backend.repository.TournamentParticipantRepository;
import com.musiguessr.backend.repository.TournamentRepository;
import com.musiguessr.backend.repository.UserRepository;
import java.time.OffsetDateTime;
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

    private final GameHistoryRepository gameHistoryRepository;
    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final TournamentRepository tournamentRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getGlobalLeaderboard(String period, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results;

        switch (period.toLowerCase()) {
            case "weekly" -> {
                OffsetDateTime since = OffsetDateTime.now().minusDays(7);
                results = gameHistoryRepository.findGlobalLeaderboardSince(since, pageable);
            }
            case "monthly" -> {
                OffsetDateTime since = OffsetDateTime.now().minusDays(30);
                results = gameHistoryRepository.findGlobalLeaderboardSince(since, pageable);
            }
            default -> results = gameHistoryRepository.findGlobalLeaderboard(pageable);
        }

        return mapToLeaderboard(results);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getPlaylistLeaderboard(Long playlistId, int limit) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = gameHistoryRepository.findPlaylistLeaderboard(playlistId, pageable);

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
