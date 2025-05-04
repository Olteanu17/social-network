package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.UserFollower;
import com.socialnetwork.backend.model.UserFollowerId;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.repository.UserFollowerRepository;
import com.socialnetwork.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/followers")
public class FollowerController {
    @Autowired
    private UserFollowerRepository followerRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId, Authentication authentication) {
        String email = authentication.getName();
        User follower = userRepository.findByEmail(email);
        if (follower == null) {
            return ResponseEntity.status(401).body("Follower not found");
        }

        User userToFollow = userRepository.findById(userId).orElse(null);
        if (userToFollow == null) {
            return ResponseEntity.badRequest().body("User to follow not found");
        }

        if (follower.getId().equals(userId)) {
            return ResponseEntity.badRequest().body("Cannot follow yourself");
        }

        UserFollowerId followerId = new UserFollowerId();
        followerId.setUserId(userId);
        followerId.setFollowerId(follower.getId());

        if (followerRepository.existsById(followerId)) {
            return ResponseEntity.badRequest().body("Already following this user");
        }

        UserFollower userFollower = new UserFollower();
        userFollower.setUserId(userId);
        userFollower.setFollowerId(follower.getId());
        userFollower.setUser(userToFollow);
        userFollower.setFollower(follower);
        followerRepository.save(userFollower);

        return ResponseEntity.ok("User followed successfully");
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId, Authentication authentication) {
        String email = authentication.getName();
        User follower = userRepository.findByEmail(email);
        if (follower == null) {
            return ResponseEntity.status(401).body("Follower not found");
        }

        UserFollowerId followerId = new UserFollowerId();
        followerId.setUserId(userId);
        followerId.setFollowerId(follower.getId());

        if (!followerRepository.existsById(followerId)) {
            return ResponseEntity.badRequest().body("Not following this user");
        }

        followerRepository.deleteById(followerId);
        return ResponseEntity.ok("User unfollowed successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getFollowersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(followerRepository.findAll().stream()
                .filter(follower -> follower.getUserId().equals(userId))
                .toList());
    }
}