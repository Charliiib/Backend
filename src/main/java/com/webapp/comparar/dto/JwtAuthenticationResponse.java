package com.webapp.comparar.dto;

public class JwtAuthenticationResponse {
    private String token;
    private UserResponse user;

    public JwtAuthenticationResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public UserResponse getUser() {
        return user;
    }
}