package com.socialnetwork.backend.repository;

import com.socialnetwork.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}