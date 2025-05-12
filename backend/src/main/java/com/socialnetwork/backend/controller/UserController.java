package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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
        return ResponseEntity.ok(users.stream().map(user -> new UserProfileDTO(user.getId(), user.getUsername())).collect(Collectors.toList()));
    }
}

class UserProfileDTO {
    private Long id;
    private String username;

    public UserProfileDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}