package com.MonitoreoViru.dto;

import com.MonitoreoViru.models.EstadoSenal;

import java.time.Instant;

/** Lectura compartida con los agricultores de una misma comisión. */
public record CanalMapaDTO(Long dispositivoId, String codigo, String nombreCanal, EstadoSenal senal,
                          Instant medidoEn, Instant vigenteHasta, Boolean simulada,
                          EstadoCaudalDTO caudal, boolean propio) {

    public static CanalMapaDTO de(DispositivoEstadoDTO dispositivo, boolean propio) {
        return new CanalMapaDTO(dispositivo.dispositivoId(), dispositivo.codigo(), dispositivo.nombreCanal(),
                dispositivo.senal(), dispositivo.medidoEn(), dispositivo.vigenteHasta(), dispositivo.simulada(),
                dispositivo.caudal(), propio);
    }
}
