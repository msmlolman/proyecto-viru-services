package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dispositivo")
@Getter
@NoArgsConstructor
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The (admin) user responsible for the device
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDispositivo tipo;

    @Column(length = 100)
    private String modelo;

    @Column(name = "dev_eui", nullable = false, unique = true, length = 16)
    private String devEui;

    @Column
    private String ubicacion;

    @Column(name = "intervalo_segundos", nullable = false)
    private int intervaloSegundos;

    @Column(name = "tolerancia_segundos", nullable = false)
    private int toleranciaSegundos;

    @Column(nullable = false)
    private boolean activo;
}
