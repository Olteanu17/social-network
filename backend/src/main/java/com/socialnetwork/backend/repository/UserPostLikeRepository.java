package com.socialnetwork.backend.repository;

import com.socialnetwork.backend.model.UserPostLike;
import com.socialnetwork.backend.model.UserPostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPostLikeRepository extends JpaRepository<UserPostLike, UserPostLikeId> {
}