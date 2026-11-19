package com.MonitoreoViru.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "viru")
@Validated
public record ViruProperties(@NotBlank String adminCode) {}