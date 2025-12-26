package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRoundRepository extends JpaRepository<GameRound, Long> {
    List<GameRound> findByGameHistoryIdOrderByRoundAsc(Long gameHistoryId);

    Optional<GameRound> findTopByGameHistoryIdOrderByRoundDesc(Long gameHistoryId);
}
