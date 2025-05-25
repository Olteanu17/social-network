package com.socialnetwork.backend.repository;

import com.socialnetwork.backend.model.UserPostLike;
import com.socialnetwork.backend.model.UserPostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserPostLikeRepository extends JpaRepository<UserPostLike, UserPostLikeId> {
    @Modifying
    @Transactional
    @Query("DELETE FROM UserPostLike upl WHERE upl.postId = ?1")
    void deleteByPostId(Long postId);
}