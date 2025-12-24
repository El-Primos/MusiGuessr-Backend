package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.GameRound;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRoundRepository extends JpaRepository<GameRound, Long> {

    List<GameRound> findByGameHistoryIdOrderByRoundAsc(Long gameHistoryId);
}
