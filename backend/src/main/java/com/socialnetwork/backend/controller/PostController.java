package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.Post;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.repository.PostRepository;
import com.socialnetwork.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        Post post = new Post();
        post.setUser(user);
        post.setContent(request.getContent());
        postRepository.save(post);
        return ResponseEntity.ok("Post created successfully");
    }

    @GetMapping
    public ResponseEntity<?> getPosts() {
        return ResponseEntity.ok(postRepository.findAll());
    }
}

class PostRequest {
    @NotBlank(message = "Content is required")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
