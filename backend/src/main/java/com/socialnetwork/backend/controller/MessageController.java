package com.socialnetwork.backend.controller;

import com.socialnetwork.backend.model.Message;
import com.socialnetwork.backend.model.User;
import com.socialnetwork.backend.repository.MessageRepository;
import com.socialnetwork.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageRequest request, Authentication authentication) {
        String email = authentication.getName();
        User sender = userRepository.findByEmail(email);
        if (sender == null) {
            return ResponseEntity.status(401).body("Sender not found");
        }

        User receiver = userRepository.findById(request.getReceiverId()).orElse(null);
        if (receiver == null) {
            return ResponseEntity.badRequest().body("Receiver not found");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());
        message.setRead(false);
        messageRepository.save(message);

        return ResponseEntity.ok("Message sent successfully");
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<?> getConversation(@PathVariable Long userId, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        List<Message> messages = messageRepository.findAll().stream()
                .filter(message ->
                        (message.getSender().getId().equals(currentUser.getId()) && message.getReceiver().getId().equals(userId)) ||
                                (message.getSender().getId().equals(userId) && message.getReceiver().getId().equals(currentUser.getId()))
                )
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/users/names")
    public ResponseEntity<?> getUserNames(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users.stream().map(user -> new UserNameDTO(user.getId(), user.getUsername())).collect(Collectors.toList()));
    }

    @GetMapping("/current-id")
    public ResponseEntity<?> getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        return ResponseEntity.ok(currentUser.getId());
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<?> editMessage(
            @PathVariable Long messageId,
            @RequestPart("content") @NotBlank String content,
            Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null) {
            return ResponseEntity.status(404).body("Message not found");
        }

        if (!message.getSender().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("Only the sender can edit this message");
        }

        message.setContent(content);
        messageRepository.save(message);
        return ResponseEntity.ok("Message updated successfully");
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null) {
            return ResponseEntity.status(404).body("Message not found");
        }

        if (!message.getSender().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("Only the sender can delete this message");
        }

        messageRepository.delete(message);
        return ResponseEntity.ok("Message deleted successfully");
    }
}

class MessageRequest {
    private Long receiverId;

    @NotBlank(message = "Content is required")
    private String content;

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

class UserNameDTO {
    private Long id;
    private String username;

    public UserNameDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
}