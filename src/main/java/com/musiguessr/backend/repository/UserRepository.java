package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.User;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);

    interface ProfileProjection {
        Long getId();
        String getName();
        String getUsername();
        String getEmail();
        Integer getTotalScore();
        Long getGamesPlayed();
        java.time.OffsetDateTime getLastPlayedAt();
    }

    interface GameHistoryProjection {
        Long getGameId();
        Long getPlaylistId();
        Integer getTotalScore();
        java.time.OffsetDateTime getPlayedAt();
    }

    interface TournamentHistoryProjection {
        Long getTournamentId();
        Integer getUserScore();
        String getStatus();
        java.time.OffsetDateTime getStartsAt();
        java.time.OffsetDateTime getEndsAt();
    }

    @Query(value = """
            SELECT
                u.id,
                u.name,
                u.username AS username,
                u.email,
                u.score AS totalScore,
                COALESCE((SELECT COUNT(*) FROM musiguessr_schema.game_history gh WHERE gh.user_id = u.id), 0) AS gamesPlayed,
                (SELECT MAX(g.played_at)
                 FROM musiguessr_schema.games g
                 JOIN musiguessr_schema.game_history gh2 ON gh2.game_id = g.id
                 WHERE gh2.user_id = u.id) AS lastPlayedAt
            FROM musiguessr_schema.users u
            WHERE u.id = :userId
            """, nativeQuery = true)
    Optional<ProfileProjection> findProfileByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT
                gh.game_id AS gameId,
                g.playlist_id AS playlistId,
                gh.user_score AS totalScore,
                g.played_at AS playedAt
            FROM musiguessr_schema.game_history gh
            JOIN musiguessr_schema.games g ON g.id = gh.game_id
            WHERE gh.user_id = :userId
            ORDER BY g.played_at DESC
            """, nativeQuery = true)
    List<GameHistoryProjection> findGameHistoryByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT
                ti.tournament_id AS tournamentId,
                ti.user_score AS userScore,
                t.name AS status, -- using name as placeholder status if not present
                t.start_date AS startsAt,
                t.end_date AS endsAt
            FROM musiguessr_schema.tournament_info ti
            JOIN musiguessr_schema.tournaments t ON t.id = ti.tournament_id
            WHERE ti.user_id = :userId
            ORDER BY t.start_date DESC NULLS LAST
            """, nativeQuery = true)
    List<TournamentHistoryProjection> findTournamentHistoryByUserId(@Param("userId") Long userId);
}
