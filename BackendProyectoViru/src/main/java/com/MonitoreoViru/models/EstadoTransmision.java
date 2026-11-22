package com.MonitoreoViru.models;

/** Stored in transaccion.estado (varchar(15)). */
public enum EstadoTransmision {
    PENDIENTE,   // slot created, waiting for the device
    RECIBIDA,    // arrived before limite_en
    TARDIA,      // arrived after limite_en
    PERDIDA      // limite_en passed with nothing received
}
