package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.Post;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.repository.PostRepository;
import com.socialnetwork.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestPart("content") @NotBlank String content,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            try {
                // Salvează imaginea ca date binare
                post.setImageData(image.getBytes());
                // Salvează tipul MIME
                post.setImageUrl(image.getContentType());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Failed to upload image: " + e.getMessage());
            }
        }

        Post savedPost = postRepository.save(post);
        // Convertește imageData în Base64 pentru răspuns
        if (savedPost.getImageData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(savedPost.getImageData());
            savedPost.setImageUrl("data:" + savedPost.getImageUrl() + ";base64," + base64Image);
        }
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping
    public ResponseEntity<?> getPosts() {
        List<Post> posts = postRepository.findAll();
        // Convertește imageData în Base64 pentru fiecare postare
        List<Post> postsWithBase64 = posts.stream().map(post -> {
            if (post.getImageData() != null) {
                String base64Image = Base64.getEncoder().encodeToString(post.getImageData());
                post.setImageUrl("data:" + post.getImageUrl() + ";base64," + base64Image);
            }
            return post;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(postsWithBase64);
    }
}