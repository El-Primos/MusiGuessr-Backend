package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.PlaylistItem;
import com.musiguessr.backend.model.PlaylistItemId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, PlaylistItemId> {

    List<PlaylistItem> findByIdPlaylistIdOrderByIdPositionAsc(Long playlistId);

    boolean existsByIdPlaylistIdAndMusicId(Long playlistId, Long musicId);

    Optional<PlaylistItem> findByIdPlaylistIdAndMusicId(Long playlistId, Long musicId);

    Integer findMaxPositionByIdPlaylistId(Long playlistId);

    Set<Long> findMusicIdsByIdPlaylistId(Long playlistId);

    Set<Integer> findPositionsByIdPlaylistId(Long playlistId);
}
