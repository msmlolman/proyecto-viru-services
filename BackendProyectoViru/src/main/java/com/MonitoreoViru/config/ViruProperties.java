package com.MonitoreoViru.config;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "viru")
@Validated
public record ViruProperties(@NotBlank String adminCode,
                             @NotBlank @Size(min = 16) String ingestApiKey,
                             @Valid Jwt jwt) {
    public record Jwt(@NotBlank @Size(min = 32) String secret, @Positive long expirationMinutes) {}
}
