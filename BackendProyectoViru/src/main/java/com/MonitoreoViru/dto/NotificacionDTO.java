package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.Entrega;
import com.MonitoreoViru.models.TipoMensaje;

import java.time.Instant;

/** What a farmer sees in the inbox. "id" is the entrega id (used to mark it as read). */
public record NotificacionDTO(
        Long id,
        TipoMensaje tipo,
        Instant riegoEn,
        String nota,
        Instant creadoEn,
        boolean leido
) {
    public static NotificacionDTO from(Entrega e) {
        return new NotificacionDTO(
                e.getId(),
                e.getMensaje().getTipo(),
                e.getMensaje().getRiegoEn(),
                e.getMensaje().getNota(),
                e.getMensaje().getCreadoEn(),
                e.getLeidoEn() != null);
    }
}
