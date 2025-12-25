package com.musiguessr.backend.service;

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

    @Data
    public static class GameSession {
        private Long gameId;
        private Long playlistId;

        private int round;
        private int totalRounds;
        private int score;

        private java.util.List<Long> orderedMusicIds;

        private RoundState currentRound;
    }

    @Data
    public static class RoundState {
        private int roundNumber;
        private Long correctMusicId;
        private String previewUrl;
        private boolean guessed;
    }
}
