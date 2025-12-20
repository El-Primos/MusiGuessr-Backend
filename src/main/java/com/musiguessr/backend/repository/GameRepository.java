package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Game;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Game g SET g.playedAt = :playedAt WHERE g.id = :gameId")
    int updatePlayedAt(@Param("gameId") Long gameId, @Param("playedAt") OffsetDateTime playedAt);
}
