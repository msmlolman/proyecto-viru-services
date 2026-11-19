package com.MonitoreoViru.dto;

import jakarta.validation.constraints.*;

public record SignUpFarmerRequestDTO(
        @NotBlank(message = "{validation.required}")
        @Size(max = 100, message = "{validation.size.max}")
        String nombre,

        @NotBlank(message = "{validation.required}")
        @Size(max = 40, message = "{validation.size.max}")
        String apellido,

        @NotBlank(message = "{validation.required}")
        @Email(message = "{validation.email.invalid}")
        String correo,

        @NotBlank(message = "{validation.required}")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "{validation.password.weak}"
        )
        String password,

        @NotNull(message = "{validation.required}")
        Integer juntaReganteId
) {}
