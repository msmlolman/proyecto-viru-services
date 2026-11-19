package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.Role;

public record UserResponseDTO(
        Long id,
        String nombre,
        String correo,
        Role role
) {}
