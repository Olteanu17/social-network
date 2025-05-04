package com.socialnetwork.backend.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_followers")
@IdClass(UserFollowerId.class)
@Data
public class UserFollower {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "follower_id")
    private Long followerId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;

    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt = LocalDateTime.now();
}