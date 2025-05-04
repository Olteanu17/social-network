package com.socialnetwork.backend.model;

import lombok.Data;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class PostTagId implements Serializable {
    private Long postId;
    private Long tagId;
}