package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Music;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MusicRepository extends JpaRepository<Music, Long> {
    boolean existsByName(String name);
}
