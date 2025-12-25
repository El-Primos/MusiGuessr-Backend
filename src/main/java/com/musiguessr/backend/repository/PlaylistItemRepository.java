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

    Integer findMaxPositionByIdPlaylistId(Long playlistId);

    Set<Long> findMusicIdsByIdPlaylistId(Long playlistId);

    Set<Integer> findPositionsByIdPlaylistId(Long playlistId);

    Long findMusicIdByIdPlaylistIdAndIdPosition(Long playlistId, Integer position);

    Long countByIdPlaylistId(Long playlistId);

    @Query(value = """
            SELECT m.* FROM playlist_items pi
            INNER JOIN musics m ON pi.music_id = m.id
            WHERE pi.playlist_id = :playlistId
            ORDER BY pi.position ASC 
            LIMIT 1 OFFSET :offset
            """, nativeQuery = true)
    Optional<Music> findMusicByPlaylistIdAndIndex(@Param("playlistId") Long playlistId, @Param("offset") int offset);
}
