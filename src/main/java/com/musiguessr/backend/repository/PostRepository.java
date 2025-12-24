package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserIdOrderByPostedAtDesc(Long userId);
}
