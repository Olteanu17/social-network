package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.Post;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.model.Tag;
import com.socialnetwork.backend.model.PostTag;
import com.socialnetwork.backend.repository.PostRepository;
import com.socialnetwork.backend.repository.UserRepository;
import com.socialnetwork.backend.repository.TagRepository;
import com.socialnetwork.backend.repository.PostTagRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostTagRepository postTagRepository;

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
                post.setImageData(image.getBytes());
                post.setImageUrl(image.getContentType());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Failed to upload image: " + e.getMessage());
            }
        }

        Post savedPost = postRepository.save(post);
        if (savedPost.getImageData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(savedPost.getImageData());
            savedPost.setImageUrl("data:" + savedPost.getImageUrl() + ";base64," + base64Image);
        }
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping
    public ResponseEntity<?> getPosts() {
        List<Post> posts = postRepository.findAll();
        List<Post> postsWithBase64 = posts.stream().map(post -> {
            if (post.getImageData() != null) {
                String base64Image = Base64.getEncoder().encodeToString(post.getImageData());
                post.setImageUrl("data:" + post.getImageUrl() + ";base64," + base64Image);
            }
            return post;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(postsWithBase64);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterPostsByTags(@RequestParam(required = false) List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return getPosts(); // Returnează toate postările dacă nu sunt tag-uri
        }

        // Găsește ID-urile tag-urilor
        List<Tag> tagEntities = tagRepository.findAll().stream()
                .filter(tag -> tags.contains(tag.getName()))
                .collect(Collectors.toList());

        if (tagEntities.size() != tags.size()) {
            return ResponseEntity.badRequest().body("One or more tags not found");
        }

        // Găsește postările care au toate tag-urile specificate
        List<PostTag> postTags = postTagRepository.findAll();
        Set<Long> postIds = null;
        for (Tag tag : tagEntities) {
            Set<Long> currentPostIds = postTags.stream()
                    .filter(pt -> pt.getTagId().equals(tag.getId()))
                    .map(PostTag::getPostId)
                    .collect(Collectors.toSet());
            if (postIds == null) {
                postIds = currentPostIds;
            } else {
                postIds.retainAll(currentPostIds); // Intersecție
            }
            if (postIds.isEmpty()) {
                break; // Nu există postări cu toate tag-urile
            }
        }

        // Obține postările
        List<Post> filteredPosts;
        if (postIds == null || postIds.isEmpty()) {
            filteredPosts = List.of(); // Listă goală dacă nu există postări
        } else {
            filteredPosts = postRepository.findAllById(postIds);
        }

        // Convertește imaginile în Base64
        List<Post> postsWithBase64 = filteredPosts.stream().map(post -> {
            if (post.getImageData() != null) {
                String base64Image = Base64.getEncoder().encodeToString(post.getImageData());
                post.setImageUrl("data:" + post.getImageUrl() + ";base64," + base64Image);
            }
            return post;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(postsWithBase64);
    }
}