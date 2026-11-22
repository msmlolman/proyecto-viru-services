package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** One measured variable (e.g. caudal, temperatura) of one transaction. */
@Entity
@Table(name = "lectura")
@Getter
@NoArgsConstructor
public class Lectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Transmision transmision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @Column(nullable = false, length = 30)
    private String variable;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal valor;

    @Column(length = 10)
    private String unidad;

    @Column(name = "medido_en", nullable = false)
    private Instant medidoEn;

    public Lectura(Transmision transmision, Dispositivo dispositivo, String variable,
                   BigDecimal valor, String unidad, Instant medidoEn) {
        this.transmision = transmision;
        this.dispositivo = dispositivo;
        this.variable = variable;
        this.valor = valor;
        this.unidad = unidad;
        this.medidoEn = medidoEn;
    }
}
