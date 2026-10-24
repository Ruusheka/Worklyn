package com.worklyn.modules.auth.dto;
public record LoginResponse(String token, String tokenType, long expiresIn, UserResponse user) { }
