package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {

    Optional<GameHistory> findByGameId(Long gameId);

    @Query("SELECT CASE WHEN COUNT(gh) > 0 THEN true ELSE false END " +
           "FROM GameHistory gh JOIN gh.game g " +
           "WHERE g.tournamentId = :tournamentId AND gh.userId = :userId")
    boolean existsByTournamentIdAndUserId(@Param("tournamentId") Long tournamentId, 
                                          @Param("userId") Long userId);
}

