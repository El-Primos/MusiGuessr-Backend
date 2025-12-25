package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {

    Optional<GameHistory> findByGameId(Long gameId);
}

