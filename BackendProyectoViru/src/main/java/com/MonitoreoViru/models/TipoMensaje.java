package com.MonitoreoViru.models;

/** Stored in mensaje.tipo (varchar(30)). */
public enum TipoMensaje {
    HORA_RIEGO,           // "hora de riego a las: ..." (needs riego_en)
    ALERTA_BAJO_CAUDAL,
    ALERTA_ALTO_CAUDAL
}
