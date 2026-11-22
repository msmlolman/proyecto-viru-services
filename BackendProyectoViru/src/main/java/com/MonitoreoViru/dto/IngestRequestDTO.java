package com.MonitoreoViru.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Body of POST /api/ingest/transacciones.
 * ASSUMED shape (adjust once you know exactly what the devices send):
 * {
 *   "devEui": "A81758FFFE05B2C1",
 *   "medidoEn": "2026-11-20T15:04:00Z",     // optional, defaults to recibidoEn
 *   "recibidoEn": "2026-11-20T15:04:02Z",   // optional, network server timestamp, defaults to now
 *   "variables": [ {"nombre":"caudal","valor":1.254,"unidad":"m3/s"} ]
 * }
 */
public record IngestRequestDTO(
        @NotBlank(message = "{validation.required}")
        @Size(max = 16, message = "{validation.size.max}")
        String devEui,

        Instant medidoEn,

        Instant recibidoEn,

        @NotEmpty(message = "{validation.required}")
        List<@Valid VariableDTO> variables
) {}
