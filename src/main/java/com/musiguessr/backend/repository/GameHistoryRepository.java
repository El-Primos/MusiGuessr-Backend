package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.GameHistory;
import com.musiguessr.backend.model.GameHistoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, GameHistoryId> {

    List<GameHistory> findByIdGameId(Long gameId);

    List<GameHistory> findByIdUserId(Long userId);
}
