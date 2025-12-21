package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Tournament;
import com.musiguessr.backend.model.TournamentStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByStatus(TournamentStatus status);
    List<Tournament> findByStatusIn(List<TournamentStatus> statuses);
    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);
}
