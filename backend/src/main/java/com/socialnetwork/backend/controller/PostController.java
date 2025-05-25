package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.Post;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.model.Tag;
import com.socialnetwork.backend.model.PostTag;
import com.socialnetwork.backend.repository.PostRepository;
import com.socialnetwork.backend.repository.UserRepository;
import com.socialnetwork.backend.repository.TagRepository;
import com.socialnetwork.backend.repository.PostTagRepository;
import com.socialnetwork.backend.repository.UserPostLikeRepository;
import com.socialnetwork.backend.repository.CommentRepository;
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

    @Autowired
    private UserPostLikeRepository userPostLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

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
            return getPosts();
        }

        List<Tag> tagEntities = tagRepository.findAll().stream()
                .filter(tag -> tags.contains(tag.getName()))
                .collect(Collectors.toList());

        if (tagEntities.size() != tags.size()) {
            return ResponseEntity.badRequest().body("One or more tags not found");
        }

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
                postIds.retainAll(currentPostIds);
            }
            if (postIds.isEmpty()) {
                break;
            }
        }

        List<Post> filteredPosts = postIds != null && !postIds.isEmpty() ? postRepository.findAllById(postIds) : List.of();
        List<Post> postsWithBase64 = filteredPosts.stream().map(post -> {
            if (post.getImageData() != null) {
                String base64Image = Base64.getEncoder().encodeToString(post.getImageData());
                post.setImageUrl("data:" + post.getImageUrl() + ";base64," + base64Image);
            }
            return post;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(postsWithBase64);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body("Post not found");
        }

        if (!post.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Only the creator can delete this post");
        }

        // Șterge relațiile asociate
        postTagRepository.deleteByPostId(postId);
        userPostLikeRepository.deleteByPostId(postId);
        commentRepository.deleteByPostId(postId);

        postRepository.delete(post);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> editPost(
            @PathVariable Long postId,
            @RequestPart("content") @NotBlank String content,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body("Post not found");
        }

        if (!post.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Only the creator can edit this post");
        }

        post.setContent(content);
        Post updatedPost = postRepository.save(post);
        if (updatedPost.getImageData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(updatedPost.getImageData());
            updatedPost.setImageUrl("data:" + updatedPost.getImageUrl() + ";base64," + base64Image);
        }
        return ResponseEntity.ok(updatedPost);
    }
}