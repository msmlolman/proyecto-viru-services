package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.AlcanceMensaje;
import com.MonitoreoViru.models.TipoMensaje;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Rules checked in MensajeService (they depend on several fields at once):
 *  - tipo HORA_RIEGO  -> riegoEn required; alerts -> riegoEn must be empty
 *  - alcance JUNTA    -> juntaReganteId required, regionId empty
 *  - alcance REGION   -> regionId required, juntaReganteId empty
 */
public record EnviarMensajeRequestDTO(
        @NotNull(message = "{validation.required}")
        TipoMensaje tipo,

        @NotNull(message = "{validation.required}")
        AlcanceMensaje alcance,

        Integer juntaReganteId,

        Integer regionId,

        Instant riegoEn,

        @Size(max = 500, message = "{validation.size.max}")
        String nota
) {}
