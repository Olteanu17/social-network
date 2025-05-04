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

        return ResponseEntity.ok(messageRepository.findBySenderIdOrReceiverId(currentUser.getId(), userId));
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