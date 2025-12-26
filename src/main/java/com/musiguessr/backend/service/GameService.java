package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.game.*;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.*;

import java.time.Instant;
import java.util.Objects;

import com.musiguessr.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final long ROUND_DURATION_MS = 30_000L;

    private final GameRepository gameRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final GameRoundRepository gameRoundRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;

    private final PlaylistService playlistService;
    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository tournamentParticipantRepository;

    @Transactional
    public GameResponseDTO createGame() {
        String playlistName = "playlist_" + Instant.now().toEpochMilli();
        Long playlistId = playlistService.createRandomPlaylist(playlistName, 5);

        Game game = new Game();
        game.setOwnerId(getUserId());
        game.setState(GameState.CREATED);
        game.setPlaylistId(playlistId);

        Game saved = gameRepository.save(game);

        if (saved.getPlaylistId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has no playlist");
        }

        long totalRounds = playlistItemRepository.countByIdPlaylistId(saved.getPlaylistId());

        return new GameResponseDTO(saved.getId(), saved.getState().name(), saved.getPlaylistId(), totalRounds);
    }

    @Transactional
    public GameResponseDTO createTournamentGame(Long tournamentId) {
        // Fetch tournament and validate it exists
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));

        // Get playlist from tournament
        Long playlistId = tournament.getPlaylistId();
        if (playlistId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tournament has no playlist");
        }

        // Validate playlist exists and has songs
        long count = playlistItemRepository.countByIdPlaylistId(playlistId);
        if (count == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist is empty or does not exist");
        }

        // Validate user is registered for the tournament
        Long userId = getUserId();
        if (userId != null && userId != 0) {
            TournamentParticipantId participantId = new TournamentParticipantId(tournamentId, userId);
            if (!tournamentParticipantRepository.existsById(participantId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "You must join the tournament before playing");
            }

            // Check if user has already played this tournament
            if (gameHistoryRepository.existsByTournamentIdAndUserId(tournamentId, userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "You have already played this tournament");
            }
        }

        Game game = new Game();
        game.setOwnerId(userId);
        game.setState(GameState.CREATED);
        game.setPlaylistId(playlistId);
        game.setTournamentId(tournamentId);

        Game saved = gameRepository.save(game);

        long totalRounds = playlistItemRepository.countByIdPlaylistId(saved.getPlaylistId());

        return new GameResponseDTO(saved.getId(), saved.getState().name(), saved.getPlaylistId(), totalRounds);
    }

    @Transactional
    public GameStartDTO startGame(Long id) {
        Game game = ensureOwnedGame(id);

        if (game.getState() == GameState.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game already started");
        } else if (game.getState() == GameState.FINISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game already finished");
        }

        Music firstSong = playlistItemRepository.findMusicByPlaylistIdAndIndex(game.getPlaylistId(), 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist is empty"));

        long totalRounds = playlistItemRepository.countByIdPlaylistId(game.getPlaylistId());

        GameHistory history = new GameHistory();
        history.setGameId(game.getId());
        history.setUserId(getUserId());
        history.setScore(0);
        gameHistoryRepository.save(history);

        game.setState(GameState.ACTIVE);
        gameRepository.save(game);

        createRound(history.getId(), 1, firstSong.getName());

        GameStartDTO gameStartDTO = new GameStartDTO();
        gameStartDTO.setId(game.getId());
        gameStartDTO.setCurrentRound(1);
        gameStartDTO.setTotalRounds(totalRounds);
        gameStartDTO.setTotalScore(0);
        gameStartDTO.setNextPreviewUrl(firstSong.getUrl());

        return gameStartDTO;
    }

    @Transactional
    public GameRoundResultDTO skip(Long id) {
        return processRoundResult(id, null, 0);
    }

    @Transactional
    public GameRoundResultDTO guess(Long id, GameRoundGuessDTO request) {
        Music guessedSong = musicRepository.findById(request.getMusicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Music not found"));

        return processRoundResult(id, guessedSong.getName(), request.getElapsedMs());
    }

    @Transactional
    public GameResultDTO finish(Long id) {
        Game game = ensureOwnedGame(id);

        if (game.getState() == GameState.CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game not started");
        } else if (game.getState() == GameState.FINISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game already finished");
        }

        GameHistory history = gameHistoryRepository.findByGameId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game History not found"));

        int finalScore = history.getScore();

        // Add the earned score to the user's total score
        Long userId = history.getUserId();
        if (userId != null && userId != 0) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            user.setScore(user.getScore() + finalScore);
            userRepository.save(user);

            // If this is a tournament game, update the tournament participant score
            if (game.getTournamentId() != null) {
                TournamentParticipantId participantId = new TournamentParticipantId(game.getTournamentId(), userId);
                TournamentParticipant participant = tournamentParticipantRepository.findById(participantId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                "You must join the tournament before playing"));
                
                // Update participant's tournament score with the game score
                participant.setUserScore(participant.getUserScore() + finalScore);
                tournamentParticipantRepository.save(participant);
            }
        }

        game.setState(GameState.FINISHED);
        gameRepository.save(game);

        GameResultDTO gameResultDTO = new GameResultDTO();
        gameResultDTO.setId(game.getId());
        gameResultDTO.setFinalScore(finalScore);
        gameResultDTO.setHistoryId(history.getId());

        return gameResultDTO;
    }

    // ---------------- helpers ----------------

    private static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isRegistered =
                auth != null &&
                        auth.isAuthenticated() &&
                        !(auth instanceof AnonymousAuthenticationToken);

        if (!isRegistered) {
            return 0L;
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.user();

        if (Objects.equals(user.getRole(), UserRole.BANNED)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Your account is disabled. Please contact support.");
        }

        return user.getId();
    }

    private Game ensureOwnedGame(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (game.getOwnerId() != 0) {
            Long userId = getUserId();

            if (userId != 0 && !Objects.equals(userId, game.getOwnerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
            }
        }

        return game;
    }

    private void validateGameState(Game game) {
        if (game.getState() == GameState.CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game not started");
        } else if (game.getState() == GameState.FINISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game already finished");
        }
    }

    private void createRound(Long historyId, int roundNumber, String songName) {
        GameRound round = new GameRound();
        round.setGameHistoryId(historyId);
        round.setRound(roundNumber);
        round.setSong(songName);
        round.setGuessedSong("");
        round.setGuessed(false);
        round.setGuessTime(0L);
        round.setScoreEarned(0);
        gameRoundRepository.save(round);
    }

    private GameRoundResultDTO processRoundResult(Long gameId, String guessedSongName, long elapsedMs) {
        Game game = ensureOwnedGame(gameId);
        validateGameState(game);

        GameHistory history = gameHistoryRepository.findByGameId(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game History not found"));

        GameRound currentRound = gameRoundRepository.findTopByGameHistoryIdOrderByRoundDesc(history.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active round found"));

       if (!currentRound.getGuessed()) {
           boolean isCorrect = guessedSongName != null && Objects.equals(guessedSongName, currentRound.getSong());
           int earnedScore = isCorrect ? scoreForElapsed(elapsedMs) : 0;
           int newTotalScore = history.getScore() + earnedScore;

           currentRound.setGuessedSong(guessedSongName != null ? guessedSongName : "");
           currentRound.setGuessed(true);
           currentRound.setScoreEarned(earnedScore);
           currentRound.setGuessTime(elapsedMs);
           gameRoundRepository.save(currentRound);

           history.setScore(newTotalScore);
           gameHistoryRepository.save(history);

           return prepareNextRound(game, history, currentRound.getRound(), isCorrect, earnedScore);
       }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already guessed/skipped");
    }

    private GameRoundResultDTO prepareNextRound(Game game, GameHistory history, int currentRoundNumber,
                                                boolean lastCorrect, int lastEarned) {

        GameRoundResultDTO roundResultDTO = new GameRoundResultDTO();
        roundResultDTO.setCorrect(lastCorrect);
        roundResultDTO.setEarnedScore(lastEarned);
        roundResultDTO.setTotalScore(history.getScore());

        Long prevMusicId = playlistItemRepository.findMusicIdByPlaylistIdAndPosition(game.getPlaylistId(),
                currentRoundNumber);
        if (prevMusicId != null) {
            roundResultDTO.setCorrectMusicId(prevMusicId);
        }

        long totalRounds = playlistItemRepository.countByIdPlaylistId(game.getPlaylistId());
        boolean isGameFinished = totalRounds <= currentRoundNumber;

        roundResultDTO.setGameFinished(isGameFinished);

        if (isGameFinished) {
            roundResultDTO.setNextRound(null);
            roundResultDTO.setNextPreviewUrl(null);

        } else {
            Music nextSong = playlistItemRepository
                    .findMusicByPlaylistIdAndIndex(game.getPlaylistId(), currentRoundNumber)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Next music not found"));

            int nextRoundNumber = currentRoundNumber + 1;

            createRound(history.getId(), nextRoundNumber, nextSong.getName());

            roundResultDTO.setNextRound(nextRoundNumber);
            roundResultDTO.setNextPreviewUrl(nextSong.getUrl());
        }

        return roundResultDTO;
    }

    private int scoreForElapsed(long elapsedMs) {
        final int max = 1000;
        final int min = 100;

        long clamped = Math.min(Math.max(elapsedMs, 0L), ROUND_DURATION_MS);
        double t = (double) clamped / (double) ROUND_DURATION_MS;
        int score = (int) Math.round(max - t * (max - min));
        return Math.max(min, Math.min(max, score));
    }
}
