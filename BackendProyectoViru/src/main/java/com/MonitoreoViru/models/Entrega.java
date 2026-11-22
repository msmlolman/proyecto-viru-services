package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** One message delivered to one farmer; carries that farmer's read status. */
@Entity
@Table(name = "entrega")
@Getter
@NoArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mensaje_id", nullable = false)
    private Mensaje mensaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agricultor_id", nullable = false)
    private Agricultor agricultor;

    @Column(name = "leido_en")
    private Instant leidoEn;

    public Entrega(Mensaje mensaje, Agricultor agricultor) {
        this.mensaje = mensaje;
        this.agricultor = agricultor;
    }

    public void marcarLeida(Instant ahora) {
        if (this.leidoEn == null) {
            this.leidoEn = ahora;
        }
    }
}
