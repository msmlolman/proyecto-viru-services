package com.MonitoreoViru.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** One measured variable, e.g. {"nombre":"caudal","valor":1.254,"unidad":"m3/s"}. */
public record VariableDTO(
        @NotBlank(message = "{validation.required}")
        @Size(max = 30, message = "{validation.size.max}")
        String nombre,

        @NotNull(message = "{validation.required}")
        BigDecimal valor,

        @Size(max = 10, message = "{validation.size.max}")
        String unidad
) {}
