package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Music;
import com.musiguessr.backend.model.PlaylistItem;
import com.musiguessr.backend.model.PlaylistItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, PlaylistItemId> {
    boolean existsByIdPlaylistIdAndMusicId(Long playlistId, Long musicId);

    List<PlaylistItem> findByIdPlaylistIdOrderByIdPositionAsc(Long playlistId);

    Optional<PlaylistItem> findByIdPlaylistIdAndMusicId(Long playlistId, Long musicId);

    Long countByIdPlaylistId(Long playlistId);

    @Query("SELECT MAX(pi.id.position) FROM PlaylistItem pi WHERE pi.id.playlistId = :playlistId")
    Integer findMaxPositionByPlaylistId(@Param("playlistId") Long playlistId);

    @Query("SELECT p.musicId FROM PlaylistItem p WHERE p.id.playlistId = :playlistId")
    Set<Long> findMusicIdsByPlaylistId(@Param("playlistId") Long playlistId);

    @Query("SELECT p.id.position FROM PlaylistItem p WHERE p.id.playlistId = :playlistId")
    Set<Integer> findPositionsByPlaylistId(Long playlistId);

    @Query("SELECT p.music.id FROM PlaylistItem p WHERE p.id.playlistId = :playlistId AND p.id.position = :position")
    Long findMusicIdByPlaylistIdAndPosition(@Param("playlistId") Long playlistId, @Param("position") int position);

    @Query(value = """
            SELECT m.* FROM musiguessr_schema.playlist_items pi
            INNER JOIN musiguessr_schema.musics m ON pi.music_id = m.id
            WHERE pi.playlist_id = :playlistId
            ORDER BY pi.position ASC 
            LIMIT 1 OFFSET :offset
            """, nativeQuery = true)
    Optional<Music> findMusicByPlaylistIdAndIndex(@Param("playlistId") Long playlistId, @Param("offset") int offset);
}
