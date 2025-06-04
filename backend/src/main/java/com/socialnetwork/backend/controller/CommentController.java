package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.Comment;
import com.socialnetwork.backend.model.Post;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.repository.CommentRepository;
import com.socialnetwork.backend.repository.PostRepository;
import com.socialnetwork.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createComment(@Valid @RequestBody CommentRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Post post = postRepository.findById(request.getPostId()).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body("Post not found");
        }

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(request.getContent());
        commentRepository.save(comment);

        return ResponseEntity.ok("Comment created successfully");
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentRepository.findAll().stream()
                .filter(comment -> comment.getPost().getId().equals(postId))
                .toList());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return ResponseEntity.status(404).body("Comment not found");
        }

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Only the creator can delete this comment");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return ResponseEntity.status(404).body("Comment not found");
        }

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Only the creator can edit this comment");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);
        return ResponseEntity.ok("Comment updated successfully");
    }
}

class CommentRequest {
    private Long postId;

    @NotBlank(message = "Content is required")
    private String content;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}