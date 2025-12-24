package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Tournament;
import com.musiguessr.backend.model.TournamentState;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByState(TournamentState state);
    List<Tournament> findByStateIn(List<TournamentState> state);
    Page<Tournament> findByState(TournamentState state, Pageable pageable);
}
