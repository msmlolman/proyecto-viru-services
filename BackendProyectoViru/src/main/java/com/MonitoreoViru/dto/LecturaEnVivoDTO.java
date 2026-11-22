package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.EstadoTransmision;
import com.MonitoreoViru.models.TipoDispositivo;

import java.time.Instant;
import java.util.List;

/** Event pushed to admins over SSE each time a transaction is stored. */
public record LecturaEnVivoDTO(
        Long dispositivoId,
        String codigo,
        TipoDispositivo tipo,
        Instant medidoEn,
        EstadoTransmision estado,
        List<VariableDTO> variables
) {}
