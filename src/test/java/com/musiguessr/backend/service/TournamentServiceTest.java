package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.tournament.TournamentCreateRequestDTO;
import com.musiguessr.backend.dto.tournament.TournamentLeaderboardEntryDTO;
import com.musiguessr.backend.dto.tournament.TournamentResponseDTO;
import com.musiguessr.backend.dto.tournament.TournamentUpdateRequestDTO;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.PlaylistRepository;
import com.musiguessr.backend.repository.TournamentParticipantRepository;
import com.musiguessr.backend.repository.TournamentRepository;
import com.musiguessr.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private TournamentParticipantRepository participantRepository;
    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TournamentService tournamentService;

    @Test
    void createTournament_ShouldCreate_WhenValid() {
        Long creatorId = 1L;
        TournamentCreateRequestDTO request = new TournamentCreateRequestDTO();
        request.setName("My Tournament");
        request.setPlaylistId(100L);

        when(playlistRepository.existsById(100L)).thenReturn(true);
        when(userRepository.existsById(creatorId)).thenReturn(true);

        Tournament saved = new Tournament();
        saved.setId(10L);
        saved.setName("My Tournament");
        saved.setState(TournamentState.UPCOMING);
        saved.setOwnerId(creatorId);

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(saved);

        TournamentResponseDTO result = tournamentService.createTournament(creatorId, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(TournamentState.UPCOMING, result.getStatus());
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void createTournament_ShouldThrow_WhenDatesInvalid() {
        Long creatorId = 1L;
        TournamentCreateRequestDTO request = new TournamentCreateRequestDTO();
        request.setName("Invalid Dates");
        request.setPlaylistId(100L);
        request.setStartDate(OffsetDateTime.now().plusDays(5));
        request.setEndDate(OffsetDateTime.now().plusDays(1));

        when(playlistRepository.existsById(100L)).thenReturn(true);
        when(userRepository.existsById(creatorId)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                tournamentService.createTournament(creatorId, request)
        );
    }

    @Test
    void getTournaments_ShouldReturnPage() {
        Pageable pageable = Pageable.unpaged();
        Tournament t1 = new Tournament();
        t1.setId(1L);
        Page<Tournament> page = new PageImpl<>(List.of(t1));

        when(tournamentRepository.findAll(pageable)).thenReturn(page);

        Page<TournamentResponseDTO> result = tournamentService.getTournaments(null, pageable);

        assertEquals(1, result.getTotalElements());
    }


    @Test
    void updateTournament_ShouldUpdate_WhenOwner() {
        Long id = 10L;
        Long ownerId = 1L;
        TournamentUpdateRequestDTO request = new TournamentUpdateRequestDTO();
        request.setName("Updated Name");

        Tournament existing = new Tournament();
        existing.setId(id);
        existing.setOwnerId(ownerId);
        existing.setName("Old Name");

        when(tournamentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(existing);

        TournamentResponseDTO result = tournamentService.updateTournament(id, ownerId, request);

        assertEquals("Updated Name", existing.getName());
    }

    @Test
    void updateTournament_ShouldThrow_WhenNotOwner() {
        Long id = 10L;
        Long userId = 99L; // Not owner
        Tournament existing = new Tournament();
        existing.setId(id);
        existing.setOwnerId(1L);

        when(tournamentRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () ->
                tournamentService.updateTournament(id, userId, new TournamentUpdateRequestDTO())
        );
    }

    @Test
    void joinTournament_ShouldAddParticipant() {
        Long userId = 1L;
        Long tournamentId = 10L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setState(TournamentState.UPCOMING);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(participantRepository.existsByIdTournamentIdAndIdUserId(tournamentId, userId)).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(new User());

        tournamentService.joinTournament(userId, tournamentId);

        verify(participantRepository).save(any(TournamentParticipant.class));
    }

    @Test
    void joinTournament_ShouldThrow_WhenFinished() {
        Long userId = 1L;
        Long tournamentId = 10L;
        Tournament tournament = new Tournament();
        tournament.setState(TournamentState.FINISHED);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                tournamentService.joinTournament(userId, tournamentId)
        );
    }

    @Test
    void leaveTournament_ShouldDeleteParticipant() {
        Long userId = 1L;
        Long tournamentId = 10L;
        Tournament tournament = new Tournament();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(participantRepository.existsById(any(TournamentParticipantId.class))).thenReturn(true);

        tournamentService.leaveTournament(userId, tournamentId);

        verify(participantRepository).deleteById(any(TournamentParticipantId.class));
    }

    @Test
    void getLeaderboard_ShouldReturnRankedList() {
        Long tournamentId = 10L;

        TournamentParticipant p1 = new TournamentParticipant();
        p1.setId(new TournamentParticipantId(tournamentId, 1L));
        p1.setUserScore(100);
        User u1 = new User();
        u1.setUsername("Winner");
        p1.setUser(u1);

        TournamentParticipant p2 = new TournamentParticipant();
        p2.setId(new TournamentParticipantId(tournamentId, 2L));
        p2.setUserScore(50);
        User u2 = new User();
        u2.setUsername("RunnerUp");
        p2.setUser(u2);

        when(tournamentRepository.existsById(tournamentId)).thenReturn(true);
        when(participantRepository.findByIdTournamentIdOrderByUserScoreDesc(tournamentId))
                .thenReturn(List.of(p1, p2));

        List<TournamentLeaderboardEntryDTO> leaderboard = tournamentService.getLeaderboard(tournamentId);

        assertEquals(2, leaderboard.size());
        assertEquals(1, leaderboard.get(0).getRank());
        assertEquals("Winner", leaderboard.get(0).getUsername());
        assertEquals(2, leaderboard.get(1).getRank());
    }
}