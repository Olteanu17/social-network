package com.socialnetwork.backend.model;

import lombok.Data;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class UserPostLikeId implements Serializable {
    private Long userId;
    private Long postId;
}