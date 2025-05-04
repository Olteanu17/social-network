package com.socialnetwork.backend.model;

import lombok.Data;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class UserFollowerId implements Serializable {
    private Long userId;
    private Long followerId;
}