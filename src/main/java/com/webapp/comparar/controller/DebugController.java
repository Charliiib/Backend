package com.webapp.comparar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/public-test")
    public ResponseEntity<String> publicTest() {
        System.out.println("✅ DEBUG: Endpoint público accedido correctamente");
        return ResponseEntity.ok("PUBLIC ENDPOINT WORKS - " + new Date());
    }

    @GetMapping("/chatbot-test")
    public ResponseEntity<String> chatbotTest() {
        System.out.println("✅ DEBUG: Chatbot test endpoint accedido correctamente");
        return ResponseEntity.ok("CHATBOT TEST WORKS - " + new Date());
    }
}