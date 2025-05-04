package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.UserPostLike;
import com.socialnetwork.backend.model.UserPostLikeId;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.model.Post;
import com.socialnetwork.backend.repository.UserPostLikeRepository;
import com.socialnetwork.backend.repository.UserRepository;
import com.socialnetwork.backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    @Autowired
    private UserPostLikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @PostMapping("/post/{postId}")
    public ResponseEntity<?> likePost(@PathVariable Long postId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body("Post not found");
        }

        UserPostLikeId likeId = new UserPostLikeId();
        likeId.setUserId(user.getId());
        likeId.setPostId(postId);

        if (likeRepository.existsById(likeId)) {
            return ResponseEntity.badRequest().body("Post already liked");
        }

        UserPostLike like = new UserPostLike();
        like.setUserId(user.getId());
        like.setPostId(postId);
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);

        return ResponseEntity.ok("Post liked successfully");
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        UserPostLikeId likeId = new UserPostLikeId();
        likeId.setUserId(user.getId());
        likeId.setPostId(postId);

        if (!likeRepository.existsById(likeId)) {
            return ResponseEntity.badRequest().body("Post not liked");
        }

        likeRepository.deleteById(likeId);
        return ResponseEntity.ok("Post unliked successfully");
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getLikesByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(likeRepository.findAll().stream()
                .filter(like -> like.getPostId().equals(postId))
                .toList());
    }
}