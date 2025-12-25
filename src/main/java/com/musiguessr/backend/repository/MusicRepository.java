package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface MusicRepository extends JpaRepository<Music, Long> {
    boolean existsByName(String name);


    interface ProfileProjection {
        Long getId();
        String getUrl();
    }

    @Query(value = """
        SELECT
            m.id,
            m.url
        FROM musiguessr_schema.musics m
        WHERE 
            (:filterGenres  = false OR m.genre_id IN (:genres))
        AND 
            (:filterArtists = false OR m.artist_id IN (:artists))
        ORDER BY RANDOM()
        LIMIT :length
        """, nativeQuery = true)
    List<ProfileProjection> findRandomMusics(
            @Param("length") Integer length,
            @Param("genres") List<Long> genres,
            @Param("filterGenres") boolean filterGenres,
            @Param("artists") List<Long> artists,
            @Param("filterArtists") boolean filterArtists
    );
}
