package com.socialnetwork.backend.repository;

import com.socialnetwork.backend.model.UserFollower;
import com.socialnetwork.backend.model.UserFollowerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFollowerRepository extends JpaRepository<UserFollower, UserFollowerId> {
}