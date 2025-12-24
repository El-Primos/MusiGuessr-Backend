package com.musiguessr.backend.repository;

import com.musiguessr.backend.model.Following;
import com.musiguessr.backend.model.FollowingId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowingRepository extends JpaRepository<Following, FollowingId> {

    boolean existsByIdUserIdAndIdFollowingId(Long userId, Long followingId);

    List<Following> findByIdFollowingId(Long userId);

    List<Following> findByIdUserIdAndAcceptedTrue(Long userId);

    List<Following> findByIdFollowingIdAndAcceptedFalse(Long userId);

    List<Following> findByIdFollowingIdAndAcceptedTrue(Long userId);
}
