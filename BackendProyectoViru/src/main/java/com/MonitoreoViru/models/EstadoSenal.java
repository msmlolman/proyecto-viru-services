package com.MonitoreoViru.models;

/** Derived status shown on the admin dashboard. Not stored. */
public enum EstadoSenal {
    EN_LINEA,    // last finished slot was RECIBIDA or TARDIA
    SIN_SENAL,   // last finished slot was PERDIDA
    SIN_DATOS    // device has no finished slot yet
}
