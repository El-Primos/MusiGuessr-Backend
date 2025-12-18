package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
