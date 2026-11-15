package com.gym.api.auth;

public record TokenResponse(String token, Long userId, String login) {
}
