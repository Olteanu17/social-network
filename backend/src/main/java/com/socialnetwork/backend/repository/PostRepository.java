package com.socialnetwork.backend.repository;

import com.socialnetwork.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}