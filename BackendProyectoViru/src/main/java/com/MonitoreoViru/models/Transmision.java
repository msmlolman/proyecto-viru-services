package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One expected check-in ("slot") of a device. Maps the existing table "transaccion".
 * recibido_nube_en and guardado_bd_en are null while the slot is PENDIENTE.
 */
@Entity
@Table(name = "transaccion")
@Getter
@NoArgsConstructor
public class Transmision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "esperado_en", nullable = false)
    private Instant esperadoEn;

    @Column(name = "limite_en", nullable = false)
    private Instant limiteEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoTransmision estado;

    @Column(name = "recibido_nube_en")
    private Instant recibidoNubeEn;

    @Column(name = "guardado_bd_en")
    private Instant guardadoBdEn;

    /** limite_en is a snapshot: esperado_en + the device's tolerance at creation time. */
    public static Transmision pendiente(Dispositivo dispositivo, Instant esperadoEn) {
        Transmision t = new Transmision();
        t.dispositivo = dispositivo;
        t.esperadoEn = esperadoEn;
        t.limiteEn = esperadoEn.plusSeconds(dispositivo.getToleranciaSegundos());
        t.estado = EstadoTransmision.PENDIENTE;
        return t;
    }

    public void registrarRecepcion(Instant recibidoNubeEn, Instant guardadoBdEn) {
        this.recibidoNubeEn = recibidoNubeEn;
        this.guardadoBdEn = guardadoBdEn;
        this.estado = recibidoNubeEn.isAfter(limiteEn)
                ? EstadoTransmision.TARDIA
                : EstadoTransmision.RECIBIDA;
    }

    public void marcarPerdida() {
        this.estado = EstadoTransmision.PERDIDA;
    }
}
