package com.webapp.comparar.controller;

import com.webapp.comparar.security.JwtTokenProvider;
import com.webapp.comparar.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> getWelcome(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            Integer userId = extractUserIdFromToken(authHeader);
            Map<String, Object> response = chatService.getWelcomeMessage(userId);
            logger.info("Welcome message generated for user: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in /welcome endpoint: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }

            Integer userId = extractUserIdFromToken(authHeader);
            Map<String, Object> response = chatService.processMessage(message.trim(), userId);

            logger.info("Chat message processed for user: {}, message: {}", userId, message);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in /chat endpoint: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error processing message"));
        }
    }

    private Integer extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No valid auth header found");
            return null;
        }

        try {
            String token = authHeader.substring(7);

            if (!jwtTokenProvider.validateToken(token)) {
                logger.warn("Invalid JWT token");
                return null;
            }

            Long userIdLong = jwtTokenProvider.getUserIdFromJWT(token);
            if (userIdLong != null) {
                logger.debug("User ID extracted from token: {}", userIdLong);
                return userIdLong.intValue();
            }

        } catch (Exception e) {
            logger.error("Error extracting user ID from token: ", e);
        }

        return null;
    }
}