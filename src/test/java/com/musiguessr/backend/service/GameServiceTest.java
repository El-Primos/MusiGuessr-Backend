package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.game.*;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.*;
import com.musiguessr.backend.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private final Long USER_ID = 1L;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameHistoryRepository gameHistoryRepository;
    @Mock
    private GameRoundRepository gameRoundRepository;
    @Mock
    private PlaylistItemRepository playlistItemRepository;
    @Mock
    private MusicRepository musicRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlaylistService playlistService;
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private TournamentParticipantRepository tournamentParticipantRepository;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(USER_ID);
        user.setRole(UserRole.USER);
        user.setScore(100);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void createGame_ShouldReturnGameDTO() {
        Long playlistId = 100L;
        when(playlistService.createRandomPlaylist(anyString(), anyInt())).thenReturn(playlistId);

        Game savedGame = new Game();
        savedGame.setId(10L);
        savedGame.setState(GameState.CREATED);
        savedGame.setPlaylistId(playlistId);
        savedGame.setOwnerId(USER_ID);

        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);
        when(playlistItemRepository.countByIdPlaylistId(playlistId)).thenReturn(5L);

        GameResponseDTO response = gameService.createGame();

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(5L, response.getTotalRounds());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void createTournamentGame_ShouldSucceed_WhenValid() {
        Long tournamentId = 50L;
        Long playlistId = 100L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setPlaylistId(playlistId);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(playlistItemRepository.countByIdPlaylistId(playlistId)).thenReturn(5L);

        TournamentParticipantId partId = new TournamentParticipantId(tournamentId, USER_ID);
        when(tournamentParticipantRepository.existsById(partId)).thenReturn(true);
        when(gameHistoryRepository.existsByTournamentIdAndUserId(tournamentId, USER_ID)).thenReturn(false);

        Game savedGame = new Game();
        savedGame.setId(20L);
        savedGame.setState(GameState.CREATED);
        savedGame.setTournamentId(tournamentId);
        savedGame.setPlaylistId(playlistId);

        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

        GameResponseDTO response = gameService.createTournamentGame(tournamentId);

        assertNotNull(response);
        assertEquals(20L, response.getId());
    }

    @Test
    void createTournamentGame_ShouldThrow_WhenNotJoined() {
        Long tournamentId = 50L;
        Tournament tournament = new Tournament();
        tournament.setPlaylistId(100L);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(playlistItemRepository.countByIdPlaylistId(100L)).thenReturn(5L);
        when(tournamentParticipantRepository.existsById(any())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> gameService.createTournamentGame(tournamentId));
    }

    @Test
    void startGame_ShouldInitializeRounds() {
        Long gameId = 10L;
        Long playlistId = 100L;

        Game game = new Game();
        game.setId(gameId);
        game.setOwnerId(USER_ID);
        game.setPlaylistId(playlistId);
        game.setState(GameState.CREATED);

        Music firstSong = new Music();
        firstSong.setName("Song 1");
        firstSong.setUrl("http://url1");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playlistItemRepository.findMusicByPlaylistIdAndIndex(playlistId, 0)).thenReturn(Optional.of(firstSong));
        when(playlistItemRepository.countByIdPlaylistId(playlistId)).thenReturn(5L);

        GameHistory history = new GameHistory();
        history.setId(99L);
        when(gameHistoryRepository.save(any(GameHistory.class))).thenReturn(history);

        GameStartDTO result = gameService.startGame(gameId);

        assertEquals(1, result.getCurrentRound());
        assertEquals("http://url1", result.getNextPreviewUrl());
        assertEquals(GameState.ACTIVE, game.getState());
        verify(gameRoundRepository).save(any(GameRound.class));
    }

    @Test
    void guess_ShouldCalculateScore_WhenCorrect() {
        Long gameId = 10L;
        Long playlistId = 100L;
        Long musicId = 55L;
        String songName = "Correct Song";

        Game game = new Game();
        game.setId(gameId);
        game.setOwnerId(USER_ID);
        game.setState(GameState.ACTIVE);
        game.setPlaylistId(playlistId);

        GameHistory history = new GameHistory();
        history.setId(99L);
        history.setScore(0);

        GameRound currentRound = new GameRound();
        currentRound.setRound(1);
        currentRound.setSong(songName);
        currentRound.setGuessed(false);

        Music correctMusic = new Music();
        correctMusic.setId(musicId);
        correctMusic.setName(songName);
        correctMusic.setUrl("url");

        Music nextSong = new Music();
        nextSong.setId(56L);
        nextSong.setName("Song 2");
        nextSong.setUrl("url2");

        when(musicRepository.findById(musicId)).thenReturn(Optional.of(correctMusic));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameHistoryRepository.findByGameId(gameId)).thenReturn(Optional.of(history));
        when(gameRoundRepository.findTopByGameHistoryIdOrderByRoundDesc(99L)).thenReturn(Optional.of(currentRound));

        when(playlistItemRepository.countByIdPlaylistId(playlistId)).thenReturn(5L);
        when(playlistItemRepository.findMusicByPlaylistIdAndIndex(playlistId, 1)).thenReturn(Optional.of(nextSong));

        GameRoundGuessDTO request = new GameRoundGuessDTO();
        request.setMusicId(musicId);
        request.setElapsedMs(5000L);

        GameRoundResultDTO result = gameService.guess(gameId, request);

        assertTrue(result.isCorrect());
        assertTrue(result.getEarnedScore() > 0);
        assertTrue(currentRound.getGuessed());
        assertEquals(songName, currentRound.getGuessedSong());
        verify(gameRoundRepository, times(2)).save(any(GameRound.class)); // Update current + Create next
    }

    @Test
    void guess_ShouldReturnZeroScore_WhenIncorrect() {
        Long gameId = 10L;
        Long musicId = 55L;

        Game game = new Game();
        game.setId(gameId);
        game.setOwnerId(USER_ID);
        game.setState(GameState.ACTIVE);
        game.setPlaylistId(100L);
        GameHistory history = new GameHistory();
        history.setId(99L);
        GameRound currentRound = new GameRound();
        currentRound.setRound(1);
        currentRound.setSong("Actual Song");
        currentRound.setGuessed(false);

        Music wrongMusic = new Music();
        wrongMusic.setId(musicId);
        wrongMusic.setName("Wrong Song");
        wrongMusic.setUrl("u");

        when(musicRepository.findById(musicId)).thenReturn(Optional.of(wrongMusic));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameHistoryRepository.findByGameId(gameId)).thenReturn(Optional.of(history));
        when(gameRoundRepository.findTopByGameHistoryIdOrderByRoundDesc(99L)).thenReturn(Optional.of(currentRound));

        when(playlistItemRepository.countByIdPlaylistId(100L)).thenReturn(5L);
        when(playlistItemRepository.findMusicByPlaylistIdAndIndex(100L, 1)).thenReturn(Optional.of(new Music()));

        GameRoundGuessDTO request = new GameRoundGuessDTO();
        request.setMusicId(musicId);
        request.setElapsedMs(1000L);

        GameRoundResultDTO result = gameService.guess(gameId, request);

        assertFalse(result.isCorrect());
        assertEquals(0, result.getEarnedScore());
    }

    @Test
    void finish_ShouldUpdateUserScore_AndReturnResult() {
        Long gameId = 10L;
        Game game = new Game();
        game.setId(gameId);
        game.setOwnerId(USER_ID);
        game.setState(GameState.ACTIVE);

        GameHistory history = new GameHistory();
        history.setId(99L);
        history.setUserId(USER_ID);
        history.setScore(500);

        User user = new User();
        user.setId(USER_ID);
        user.setScore(100);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameHistoryRepository.findByGameId(gameId)).thenReturn(Optional.of(history));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        GameResultDTO result = gameService.finish(gameId);

        assertEquals(GameState.FINISHED, game.getState());
        assertEquals(600, user.getScore()); // 100 + 500
        assertEquals(500, result.getFinalScore());
        verify(userRepository).save(user);
    }

    @Test
    void finish_ShouldUpdateTournamentParticipantScore_WhenTournamentGame() {
        Long gameId = 10L;
        Long tournamentId = 50L;

        Game game = new Game();
        game.setId(gameId);
        game.setOwnerId(USER_ID);
        game.setTournamentId(tournamentId);
        game.setState(GameState.ACTIVE);

        GameHistory history = new GameHistory();
        history.setUserId(USER_ID);
        history.setScore(500);

        User user = new User();
        user.setId(USER_ID);
        user.setScore(0);

        TournamentParticipant participant = new TournamentParticipant();
        participant.setUserScore(200);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameHistoryRepository.findByGameId(gameId)).thenReturn(Optional.of(history));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        TournamentParticipantId partId = new TournamentParticipantId(tournamentId, USER_ID);
        when(tournamentParticipantRepository.findById(partId)).thenReturn(Optional.of(participant));

        gameService.finish(gameId);

        assertEquals(700, participant.getUserScore()); // 200 + 500
        verify(tournamentParticipantRepository).save(participant);
    }
}