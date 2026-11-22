package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.AlcanceMensaje;
import com.MonitoreoViru.models.TipoMensaje;

import java.time.Instant;

public record MensajeEnviadoDTO(
        Long id,
        TipoMensaje tipo,
        AlcanceMensaje alcance,
        int destinatarios,
        Instant creadoEn
) {}
