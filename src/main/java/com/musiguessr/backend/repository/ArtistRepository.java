package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    boolean existsByName(String name);
}
