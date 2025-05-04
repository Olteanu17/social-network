package com.socialnetwork.backend.model;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "post_tags")
@IdClass(PostTagId.class)
@Data
public class PostTag {
    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "tag_id")
    private Long tagId;

    @ManyToOne
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;
}