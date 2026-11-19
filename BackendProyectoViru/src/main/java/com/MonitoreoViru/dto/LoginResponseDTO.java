package com.MonitoreoViru.dto;

public record LoginResponseDTO(
        String token,
        String tokenType,
        long expiresInSeconds,
        UserResponseDTO user
) {}