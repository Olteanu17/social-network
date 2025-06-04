package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.*;
import com.socialnetwork.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserFollowerRepository userFollowerRepository;

    @Autowired
    private UserPostLikeRepository userPostLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Current user not found");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        User responseUser = new User();
        responseUser.setId(user.getId());
        responseUser.setUsername(user.getUsername());
        responseUser.setEmail(user.getEmail());
        responseUser.setBio(user.getBio());
        responseUser.setCreatedAt(user.getCreatedAt());
        responseUser.setAdmin(user.isAdmin());

        return ResponseEntity.ok(responseUser);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        User responseUser = new User();
        responseUser.setId(user.getId());
        responseUser.setUsername(user.getUsername());
        responseUser.setEmail(user.getEmail());
        responseUser.setBio(user.getBio());
        responseUser.setCreatedAt(user.getCreatedAt());
        responseUser.setAdmin(user.isAdmin());

        return ResponseEntity.ok(responseUser);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            @RequestPart(value = "bio", required = false) String bio,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        if (bio != null) {
            user.setBio(bio);
        }

        userRepository.save(user);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PutMapping("/{userId}/make-admin")
    public ResponseEntity<?> makeAdmin(@PathVariable Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body("Only admins can make other users admins");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        user.setAdmin(true);
        userRepository.save(user);
        return ResponseEntity.ok("User is now an admin");
    }

    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(403).body("Only admins can delete users");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        try {

            List<Post> userPosts = postRepository.findByUser(user);
            for (Post post : userPosts) {

                commentRepository.deleteByPostId(post.getId());

                userPostLikeRepository.deleteByPostId(post.getId());

                postTagRepository.deleteByPostId(post.getId());
            }

            postRepository.deleteAll(userPosts);

            commentRepository.deleteByUser(user);

            userPostLikeRepository.deleteAll(
                    userPostLikeRepository.findAll().stream()
                            .filter(like -> like.getUserId().equals(userId))
                            .collect(Collectors.toList())
            );

            messageRepository.deleteBySender(user);

            messageRepository.deleteByReceiver(user);

            userFollowerRepository.deleteByFollowerId(userId);

            userFollowerRepository.deleteByUserId(userId);

            userRepository.delete(user);

            return ResponseEntity.ok("User and all associated data deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while deleting the user: " + e.getMessage());
        }
    }

    @GetMapping("/names")
    public ResponseEntity<?> getUserNames(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Current user not found");
        }

        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users.stream().map(user -> new UserProfileDTO(user.getId(), user.getUsername(), user.isAdmin())).collect(Collectors.toList()));
    }
}

class UserProfileDTO {
    private Long id;
    private String username;
    private boolean isAdmin;

    public UserProfileDTO(Long id, String username, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.isAdmin = isAdmin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { this.isAdmin = admin; }
}