package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    Optional<User> findByUserName(String userName);

    interface ProfileProjection {
        Long getId();
        String getName();
        String getUserName();
        String getEmail();
        Integer getTotalScore();
        Long getGamesPlayed();
        java.time.OffsetDateTime getLastPlayedAt();
    }

    @Query(value = """
            SELECT
                u.id,
                u.name,
                u.user_name AS userName,
                u.email,
                u.score AS totalScore,
                COALESCE((SELECT COUNT(*) FROM game_history gh WHERE gh.user_id = u.id), 0) AS gamesPlayed,
                (SELECT MAX(g.played_at)
                 FROM games g
                 JOIN game_history gh2 ON gh2.game_id = g.id
                 WHERE gh2.user_id = u.id) AS lastPlayedAt
            FROM users u
            WHERE u.id = :userId
            """, nativeQuery = true)
    Optional<ProfileProjection> findProfileByUserId(@Param("userId") Long userId);
}
