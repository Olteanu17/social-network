package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.Tag;
import com.socialnetwork.backend.model.PostTag;
import com.socialnetwork.backend.model.PostTagId;
import com.socialnetwork.backend.model.Post;
import com.socialnetwork.backend.repository.TagRepository;
import com.socialnetwork.backend.repository.PostTagRepository;
import com.socialnetwork.backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    private PostRepository postRepository;

    @PostMapping
    public ResponseEntity<?> createTag(@Valid @RequestBody TagRequest request, Authentication authentication) {
        String email = authentication.getName();
        // Verifică dacă utilizatorul este autentificat
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(email)) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        Tag existingTag = tagRepository.findByName(request.getName());
        if (existingTag != null) {
            return ResponseEntity.badRequest().body("Tag already exists");
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tagRepository.save(tag);

        return ResponseEntity.ok("Tag created successfully");
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<?> addTagToPost(@PathVariable Long postId, @Valid @RequestBody TagRequest request, Authentication authentication) {
        String email = authentication.getName();
        // Verifică dacă utilizatorul este autentificat
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(email)) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body("Post not found");
        }

        // Verifică dacă utilizatorul este creatorul postării
        if (!post.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("Only the post creator can add tags");
        }

        Tag tag = tagRepository.findByName(request.getName());
        if (tag == null) {
            tag = new Tag();
            tag.setName(request.getName());
            tagRepository.save(tag);
        }

        PostTagId postTagId = new PostTagId();
        postTagId.setPostId(postId);
        postTagId.setTagId(tag.getId());

        if (postTagRepository.existsById(postTagId)) {
            return ResponseEntity.badRequest().body("Tag already added to post");
        }

        PostTag postTag = new PostTag();
        postTag.setPostId(postId);
        postTag.setTagId(tag.getId());
        postTag.setPost(post);
        postTag.setTag(tag);
        postTagRepository.save(postTag);

        return ResponseEntity.ok("Tag added to post successfully");
    }

    @DeleteMapping("/post/{postId}/tag/{tagId}")
    public ResponseEntity<?> removeTagFromPost(@PathVariable Long postId, @PathVariable Long tagId, Authentication authentication) {
        String email = authentication.getName();
        // Verifică dacă utilizatorul este autentificat
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(email)) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body("Post not found");
        }

        // Verifică dacă utilizatorul este creatorul postării
        if (!post.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("Only the post creator can remove tags");
        }

        PostTagId postTagId = new PostTagId();
        postTagId.setPostId(postId);
        postTagId.setTagId(tagId);

        if (!postTagRepository.existsById(postTagId)) {
            return ResponseEntity.badRequest().body("Tag not associated with post");
        }

        postTagRepository.deleteById(postTagId);
        return ResponseEntity.ok("Tag removed from post successfully");
    }

    @PutMapping("/post/{postId}/tag/{tagId}")
    public ResponseEntity<?> updateTagInPost(@PathVariable Long postId, @PathVariable Long tagId, @Valid @RequestBody TagRequest request, Authentication authentication) {
        String email = authentication.getName();
        // Verifică dacă utilizatorul este autentificat
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(email)) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.status(404).body("Post not found");
        }

        // Verifică dacă utilizatorul este creatorul postării
        if (!post.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("Only the post creator can edit tags");
        }

        PostTagId postTagId = new PostTagId();
        postTagId.setPostId(postId);
        postTagId.setTagId(tagId);

        if (!postTagRepository.existsById(postTagId)) {
            return ResponseEntity.badRequest().body("Tag not associated with post");
        }

        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (tag == null) {
            return ResponseEntity.status(404).body("Tag not found");
        }

        tag.setName(request.getName());
        tagRepository.save(tag);

        return ResponseEntity.ok("Tag updated successfully");
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getTagsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postTagRepository.findAll().stream()
                .filter(postTag -> postTag.getPostId().equals(postId))
                .map(PostTag::getTag)
                .toList());
    }

    @GetMapping
    public ResponseEntity<?> getAllTags() {
        List<String> tagNames = tagRepository.findAll().stream()
                .map(Tag::getName)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(tagNames);
    }
}

class TagRequest {
    @NotBlank(message = "Tag name is required")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}