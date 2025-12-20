package com.musiguessr.backend.service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class GameSessionStore {

    private final Map<Long, GameSession> sessions = new ConcurrentHashMap<>();

    public Optional<GameSession> get(Long gameId) {
        return Optional.ofNullable(sessions.get(gameId));
    }

    public void put(Long gameId, GameSession session) {
        sessions.put(gameId, session);
    }

    public void remove(Long gameId) {
        sessions.remove(gameId);
    }

    public boolean isActive(Long gameId) {
        return sessions.containsKey(gameId);
    }

    @Data
    public static class GameSession {
        private Long gameId;
        private Long userId;
        private Long playlistId;

        private OffsetDateTime startedAt;
        private int round;
        private int totalScore;

        private java.util.List<Long> orderedSongIds;

        private RoundState currentRound;
    }

    @Data
    public static class RoundState {
        private int roundNumber;
        private Long correctSongId;
        private String previewUrl;
        private java.util.List<Long> candidateSongIds;
        private OffsetDateTime deadlineAt;
        private boolean guessed;
    }
}
