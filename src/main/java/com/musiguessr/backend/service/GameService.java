package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.game.*;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final long ROUND_DURATION_MS = 30_000L;
    private static final int CANDIDATE_COUNT = 4;

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;

    private final GameSessionStore sessionStore;

    @Transactional
    public GameDTO createGame(Long authUserId, GameCreateRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getPlaylistId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playlistId is required");
        }
        if (!playlistRepository.existsById(request.getPlaylistId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required");
        }
        if (!"singleplayer".equalsIgnoreCase(request.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported game type");
        }

        if (!userRepository.existsById(authUserId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        Game game = new Game();
        game.setOwnerId(authUserId);
        game.setPlaylistId(request.getPlaylistId());

        Game saved = gameRepository.save(game);
        return mapGameDTO(saved, "planned", 0, null);
    }

    @Transactional
    public GameDTO startGame(Long authUserId, Long gameId) {
        Game game = ensureOwnedGame(authUserId, gameId);

        if (isCompleted(authUserId, gameId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already completed");
        }
        if (sessionStore.isActive(gameId)) {
            GameSessionStore.GameSession s = sessionStore.get(gameId).orElseThrow();
            return mapGameDTO(game, "active", s.getTotalScore(), s.getStartedAt());
        }

        if (game.getPlaylistId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has no playlist");
        }

        List<Long> orderedSongIds = playlistItemRepository
                .findByIdPlaylistIdOrderByIdPositionAsc(game.getPlaylistId())
                .stream()
                .map(PlaylistItem::getMusic)
                .filter(Objects::nonNull)
                .map(Music::getId)
                .collect(Collectors.toList());

        if (orderedSongIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Playlist is empty");
        }

        GameSessionStore.GameSession session = new GameSessionStore.GameSession();
        session.setGameId(gameId);
        session.setUserId(authUserId);
        session.setPlaylistId(game.getPlaylistId());
        session.setStartedAt(OffsetDateTime.now());
        session.setRound(0);
        session.setTotalScore(0);
        session.setOrderedSongIds(orderedSongIds);

        sessionStore.put(gameId, session);

        return mapGameDTO(game, "active", 0, session.getStartedAt());
    }

    @Transactional(readOnly = true)
    public GameDTO getGame(Long authUserId, Long gameId) {
        Game game = ensureOwnedGame(authUserId, gameId);

        if (isCompleted(authUserId, gameId)) {
            int score = gameHistoryRepository.findByGameIdAndUserId(gameId, authUserId)
                    .map(GameHistory::getScore)
                    .orElse(0);
            return mapGameDTO(game, "completed", score, null);
        }

        return sessionStore.get(gameId)
                .map(s -> mapGameDTO(game, "active", s.getTotalScore(), s.getStartedAt()))
                .orElseGet(() -> mapGameDTO(game, "planned", 0, null));
    }

    @Transactional(readOnly = true)
    public List<GameDTO> getGames(Long authUserId, Long userId, String status, Integer limit, Integer offset) {
        Stream<Game> stream = gameRepository.findAll().stream();

        Long effectiveUserId = (userId != null) ? userId : authUserId;
        if (effectiveUserId != null) {
            stream = stream.filter(g -> Objects.equals(g.getOwnerId(), effectiveUserId));
        }

        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        int safeLimit = (limit == null || limit < 0) ? 50 : limit;

        List<GameDTO> mapped = stream
                .map(g -> mapGameForList(authUserId, g))
                .collect(Collectors.toList());

        if (status != null && !status.isBlank()) {
            String needle = status.trim().toLowerCase();
            mapped = mapped.stream()
                    .filter(d -> d.getStatus() != null && d.getStatus().toLowerCase().equals(needle))
                    .toList();
        }

        return mapped.stream()
                .skip(safeOffset)
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    @Transactional
    public GameNextDTO next(Long authUserId, Long gameId) {
        ensureOwnedGame(authUserId, gameId);

        if (isCompleted(authUserId, gameId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already completed");
        }

        GameSessionStore.GameSession session = sessionStore.get(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Game is not active"));

        if (!Objects.equals(session.getUserId(), authUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        int nextRound = session.getRound() + 1;
        List<Long> ordered = session.getOrderedSongIds();

        if (nextRound > ordered.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No more rounds");
        }

        Long correctSongId = ordered.get(nextRound - 1);

        Music correct = musicRepository.findById(correctSongId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

        List<Long> candidateIds = buildCandidateIds(correctSongId, ordered);
        Collections.shuffle(candidateIds);

        OffsetDateTime deadlineAt = OffsetDateTime.now().plusNanos(ROUND_DURATION_MS * 1_000_000L);

        GameSessionStore.RoundState rs = new GameSessionStore.RoundState();
        rs.setRoundNumber(nextRound);
        rs.setCorrectSongId(correctSongId);
        rs.setPreviewUrl(correct.getUrl());
        rs.setCandidateSongIds(candidateIds);
        rs.setDeadlineAt(deadlineAt);
        rs.setGuessed(false);

        session.setRound(nextRound);
        session.setCurrentRound(rs);
        sessionStore.put(gameId, session);

        return new GameNextDTO(
                nextRound,
                correct.getUrl(),
                deadlineAt,
                candidateIds.stream()
                        .map(this::mapCandidate)
                        .collect(Collectors.toList())
        );
    }

    @Transactional
    public GameGuessDTO.Response guess(Long authUserId, Long gameId, GameGuessDTO.Request request) {
        ensureOwnedGame(authUserId, gameId);

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getSongId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "songId is required");
        }
        if (request.getElapsedMs() == null || request.getElapsedMs() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "elapsedMs must be >= 0");
        }

        if (isCompleted(authUserId, gameId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already completed");
        }

        GameSessionStore.GameSession session = sessionStore.get(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Game is not active"));

        GameSessionStore.RoundState round = session.getCurrentRound();
        if (round == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Call /next before guessing");
        }
        if (round.isGuessed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already guessed for this round");
        }
        if (OffsetDateTime.now().isAfter(round.getDeadlineAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Round deadline passed");
        }
        if (!round.getCandidateSongIds().contains(request.getSongId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "songId is not a valid candidate");
        }

        boolean correct = Objects.equals(request.getSongId(), round.getCorrectSongId());
        int gained = correct ? scoreForElapsed(request.getElapsedMs()) : 0;

        session.setTotalScore(session.getTotalScore() + gained);
        round.setGuessed(true);

        sessionStore.put(gameId, session);

        return new GameGuessDTO.Response(correct, gained, session.getTotalScore());
    }

    @Transactional
    public GameDTO finish(Long authUserId, Long gameId) {
        Game game = ensureOwnedGame(authUserId, gameId);

        if (isCompleted(authUserId, gameId)) {
            int score = gameHistoryRepository.findByGameIdAndUserId(gameId, authUserId)
                    .map(GameHistory::getScore)
                    .orElse(0);
            return mapGameDTO(game, "completed", score, null);
        }

        GameSessionStore.GameSession session = sessionStore.get(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Game is not active"));

        if (!Objects.equals(session.getUserId(), authUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        int finalScore = session.getTotalScore();

        GameHistory gh = new GameHistory();
        gh.setGameId(gameId);
        gh.setGame(game);
        gh.setUserId(authUserId);
        gh.setUser(userRepository.getReferenceById(authUserId));
        gh.setScore(finalScore);

        gameHistoryRepository.save(gh);

        game.setState(GameState.FINISHED);
        gameRepository.save(game);

        sessionStore.remove(gameId);

        Game refreshed = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        return mapGameDTO(refreshed, "completed", finalScore, null);
    }

    @Transactional(readOnly = true)
    public List<GameResultDTO> getGameResults(Long authUserId, Long gameId) {
        ensureOwnedGame(authUserId, gameId);

        return gameHistoryRepository.findByGameId(gameId).stream()
                .map(gh -> {
                    Game g = gh.getGame();
                    Long playlistId = (g != null) ? g.getPlaylistId() : null;
                    OffsetDateTime playedAt = (g != null) ? g.getCreatedAt() : null;
                    return new GameResultDTO(
                            gh.getGameId(),
                            gh.getUserId(),
                            gh.getScore(),
                            playedAt,
                            playlistId
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GameResultDTO> getUserGameResults(Long authUserId, Long userId) {
        Long effectiveUserId = (userId != null) ? userId : authUserId;

        return gameHistoryRepository.findByUserId(effectiveUserId).stream()
                .map(gh -> {
                    Game g = gh.getGame();
                    Long playlistId = (g != null) ? g.getPlaylistId() : null;
                    OffsetDateTime playedAt = (g != null) ? g.getCreatedAt() : null;
                    return new GameResultDTO(
                            gh.getGameId(),
                            gh.getUserId(),
                            gh.getScore(),
                            playedAt,
                            playlistId
                    );
                })
                .collect(Collectors.toList());
    }

    // ---------------- helpers ----------------

    private Game ensureOwnedGame(Long authUserId, Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (!Objects.equals(game.getOwnerId(), authUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return game;
    }

    private boolean isCompleted(Long authUserId, Long gameId) {
        return gameHistoryRepository.existsByGameIdAndUserId(gameId, authUserId);
    }

    private GameDTO mapGameForList(Long authUserId, Game g) {
        if (gameHistoryRepository.existsByGameIdAndUserId(g.getId(), authUserId)) {
            int score = gameHistoryRepository.findByGameIdAndUserId(g.getId(), authUserId)
                    .map(GameHistory::getScore)
                    .orElse(0);
            return mapGameDTO(g, "completed", score, null);
        }
        return sessionStore.get(g.getId())
                .map(s -> mapGameDTO(g, "active", s.getTotalScore(), s.getStartedAt()))
                .orElseGet(() -> mapGameDTO(g, "planned", 0, null));
    }

    private GameDTO mapGameDTO(Game game, String status, Integer totalScore, OffsetDateTime startedAt) {
        return new GameDTO(
                game.getId(),
                status,
                game.getPlaylistId(),
                totalScore,
                startedAt,
                game.getCreatedAt()
        );
    }

    private GameNextDTO.Candidate mapCandidate(Long songId) {
        Music m = musicRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

        String artistName = (m.getArtist() != null) ? m.getArtist().getName() : null;
        return new GameNextDTO.Candidate(m.getId(), m.getName(), artistName);
    }

    private List<Long> buildCandidateIds(Long correctSongId, List<Long> playlistSongIds) {
        List<Long> candidates = new ArrayList<>();
        candidates.add(correctSongId);

        List<Long> othersFromPlaylist = playlistSongIds.stream()
                .filter(id -> !Objects.equals(id, correctSongId))
                .distinct()
                .collect(Collectors.toList());
        Collections.shuffle(othersFromPlaylist);

        for (Long id : othersFromPlaylist) {
            if (candidates.size() >= CANDIDATE_COUNT) break;
            candidates.add(id);
        }

        if (candidates.size() >= CANDIDATE_COUNT) return candidates;

        List<Long> global = musicRepository.findAll().stream()
                .map(Music::getId)
                .filter(id -> !candidates.contains(id))
                .collect(Collectors.toList());
        Collections.shuffle(global);

        for (Long id : global) {
            if (candidates.size() >= CANDIDATE_COUNT) break;
            candidates.add(id);
        }

        return candidates;
    }

    private int scoreForElapsed(Long elapsedMs) {
        final int max = 1000;
        final int min = 100;

        long clamped = Math.min(Math.max(elapsedMs, 0L), ROUND_DURATION_MS);
        double t = (double) clamped / (double) ROUND_DURATION_MS;
        int score = (int) Math.round(max - t * (max - min));
        return Math.max(min, Math.min(max, score));
    }
}
