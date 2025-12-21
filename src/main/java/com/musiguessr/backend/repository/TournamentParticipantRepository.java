package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.TournamentParticipant;
import com.musiguessr.backend.model.TournamentParticipantId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, TournamentParticipantId> {
    List<TournamentParticipant> findByIdTournamentIdOrderByUserScoreDesc(Long tournamentId);
    int countByIdTournamentId(Long tournamentId);
    boolean existsByIdTournamentIdAndIdUserId(Long tournamentId, Long userId);
    List<TournamentParticipant> findByIdUserId(Long userId);
}
