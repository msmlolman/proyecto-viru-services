package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.EstadoSenal;
import com.MonitoreoViru.models.TipoDispositivo;

import java.time.Instant;
import java.util.List;

/** One row of the admin dashboard snapshot: a device and its latest values. */
public record DispositivoEstadoDTO(
        Long dispositivoId,
        String codigo,
        TipoDispositivo tipo,
        String modelo,
        String ubicacion,
        EstadoSenal senal,
        Instant medidoEn,
        List<VariableDTO> variables
) {}
