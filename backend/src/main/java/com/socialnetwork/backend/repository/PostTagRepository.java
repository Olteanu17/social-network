package com.socialnetwork.backend.repository;

import com.socialnetwork.backend.model.PostTag;
import com.socialnetwork.backend.model.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {
}