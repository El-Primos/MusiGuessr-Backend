package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.tournament.*;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @Transactional
    public TournamentResponseDTO createTournament(Long creatorId, TournamentCreateRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (request.getPlaylistId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playlistId is required");
        }
        if (!playlistRepository.existsById(request.getPlaylistId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
        }
        if (!userRepository.existsById(creatorId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        // Validate dates
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
            }
        }

        Tournament tournament = new Tournament();
        tournament.setOwnerId(creatorId);
        tournament.setPlaylistId(request.getPlaylistId());
        tournament.setName(request.getName());
        tournament.setDescription(request.getDescription());
        tournament.setState(TournamentState.UPCOMING);
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());
        tournament.setCreatedAt(OffsetDateTime.now());

        Tournament saved = tournamentRepository.save(tournament);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public TournamentResponseDTO getTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));
        return mapToResponseDTO(tournament);
    }

    @Transactional(readOnly = true)
    public Page<TournamentResponseDTO> getTournaments(TournamentState status, Pageable pageable) {
        Page<Tournament> tournaments;
        if (status != null) {
            tournaments = tournamentRepository.findByState(status, pageable);
        } else {
            tournaments = tournamentRepository.findAll(pageable);
        }
        return tournaments.map(this::mapToResponseDTO);
    }

    @Transactional
    public TournamentResponseDTO updateTournament(Long id, Long requesterId, TournamentUpdateRequestDTO request) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));

        if (!tournament.getOwnerId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this tournament");
        }

        OffsetDateTime currentStart = tournament.getStartDate();
        OffsetDateTime currentEnd = tournament.getEndDate();
        OffsetDateTime updatedStart = request.getStartDate() != null ? request.getStartDate() : currentStart;
        OffsetDateTime updatedEnd = request.getEndDate() != null ? request.getEndDate() : currentEnd;

        if (updatedStart != null && updatedEnd != null && updatedStart.isAfter(updatedEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            tournament.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tournament.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            tournament.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            tournament.setEndDate(request.getEndDate());
        }

        Tournament saved = tournamentRepository.save(tournament);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public TournamentResponseDTO updateTournamentState(Long id, Long requesterId, TournamentState newState) {
        if (newState == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "state is required");
        }

        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));

        if (!tournament.getOwnerId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this tournament");
        }

        tournament.setState(newState);
        Tournament saved = tournamentRepository.save(tournament);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public TournamentResponseDTO joinTournament(Long userId, Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (tournament.getState() != TournamentState.UPCOMING && tournament.getState() != TournamentState.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot join a finished tournament");
        }

        if (participantRepository.existsByIdTournamentIdAndIdUserId(tournamentId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already joined this tournament");
        }

        TournamentParticipant participant = new TournamentParticipant();
        participant.setId(new TournamentParticipantId(tournamentId, userId));
        participant.setTournament(tournament);
        participant.setUser(userRepository.getReferenceById(userId));
        participant.setUserScore(0);

        participantRepository.save(participant);

        return mapToResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO leaveTournament(Long userId, Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));

        TournamentParticipantId id = new TournamentParticipantId(tournamentId, userId);
        if (!participantRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is not a participant of this tournament");
        }

        participantRepository.deleteById(id);
        return mapToResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO startTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));

        if (tournament.getState() != TournamentState.UPCOMING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tournament is not in UPCOMING status");
        }

        tournament.setState(TournamentState.ACTIVE);
        tournament.setStartDate(OffsetDateTime.now());

        Tournament saved = tournamentRepository.save(tournament);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public TournamentResponseDTO endTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));

        if (tournament.getState() != TournamentState.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tournament is not in ACTIVE status");
        }

        tournament.setState(TournamentState.FINISHED);
        tournament.setEndDate(OffsetDateTime.now());

        Tournament saved = tournamentRepository.save(tournament);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<TournamentParticipantDTO> getParticipants(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found");
        }

        return participantRepository.findByIdTournamentIdOrderByUserScoreDesc(tournamentId).stream()
                .map(this::mapToParticipantDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TournamentLeaderboardEntryDTO> getLeaderboard(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found");
        }

        List<TournamentParticipant> participants = 
                participantRepository.findByIdTournamentIdOrderByUserScoreDesc(tournamentId);

        return IntStream.range(0, participants.size())
                .mapToObj(i -> {
                    TournamentParticipant p = participants.get(i);
                    User user = p.getUser();
                    return new TournamentLeaderboardEntryDTO(
                            i + 1,
                            p.getId().getUserId(),
                            user != null ? user.getUsername() : null,
                            p.getUserScore()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateParticipantScore(Long tournamentId, Long userId, Integer scoreToAdd) {
        TournamentParticipantId id = new TournamentParticipantId(tournamentId, userId);
        TournamentParticipant participant = participantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));

        participant.setUserScore(participant.getUserScore() + scoreToAdd);
        participantRepository.save(participant);
    }

    @Transactional
    public void deleteTournament(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found");
        }
        tournamentRepository.deleteById(tournamentId);
    }

    // ---------------- helpers ----------------

    private TournamentResponseDTO mapToResponseDTO(Tournament tournament) {
        User creator = tournament.getOwner();
        String creatorUsername = null;
        if (creator == null && tournament.getOwnerId() != null) {
            creator = userRepository.findById(tournament.getOwnerId()).orElse(null);
        }
        if (creator != null) {
            creatorUsername = creator.getUsername();
        }

        int participantCount = participantRepository.countByIdTournamentId(tournament.getId());

        return new TournamentResponseDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getDescription(),
                tournament.getPlaylistId(),
                tournament.getOwnerId(),
                creatorUsername,
                tournament.getState(),
                tournament.getCreatedAt(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                participantCount
        );
    }

    private TournamentParticipantDTO mapToParticipantDTO(TournamentParticipant participant) {
        User user = participant.getUser();
        return new TournamentParticipantDTO(
                participant.getId().getUserId(),
                user != null ? user.getUsername() : null,
                participant.getUserScore()
        );
    }
}
