package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.PlaylistItem;
import com.musiguessr.backend.model.PlaylistItemId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, PlaylistItemId> {

    boolean existsByIdPlaylistIdAndIdSongId(Long playlistId, Long songId);

    List<PlaylistItem> findByIdPlaylistIdOrderByPositionAsc(Long playlistId);

    Optional<PlaylistItem> findByIdPlaylistIdAndIdSongId(Long playlistId, Long songId);

    void deleteByIdPlaylistIdAndIdSongId(Long playlistId, Long songId);
}
