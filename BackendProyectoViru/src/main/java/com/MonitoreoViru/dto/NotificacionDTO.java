package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.Entrega;
import com.MonitoreoViru.models.Mensaje;
import com.MonitoreoViru.models.TipoMensaje;

import java.time.Instant;

/** What a farmer sees in the inbox. "id" is the entrega id (used to mark it as read). */
public record NotificacionDTO(
        Long id,
        TipoMensaje tipo,
        Instant riegoEn,
        String nota,
        Instant creadoEn,
        boolean leido,
        String canalCodigo,
        boolean simulada,
        Instant medidoEn
) {
    public static NotificacionDTO from(Entrega e) {
        Mensaje mensaje = e.getMensaje();
        return new NotificacionDTO(
                e.getId(),
                mensaje.getTipo(),
                mensaje.getRiegoEn(),
                mensaje.getNota(),
                mensaje.getCreadoEn(),
                e.getLeidoEn() != null,
                mensaje.getCanal() == null ? null : mensaje.getCanal().getCodigo(),
                mensaje.isSimulada(),
                mensaje.getMedidoEn());
    }
}
