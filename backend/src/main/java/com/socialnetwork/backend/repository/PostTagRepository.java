package com.socialnetwork.backend.repository;

import com.socialnetwork.backend.model.PostTag;
import com.socialnetwork.backend.model.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PostTag pt WHERE pt.postId = ?1")
    void deleteByPostId(Long postId);
}