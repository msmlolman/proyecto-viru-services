package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "mensaje")
@Getter
@NoArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private User autor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMensaje tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlcanceMensaje alcance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "junta_regante_id")
    private JuntaRegante juntaRegante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_id")
    private Dispositivo canal;

    @Column(nullable = false)
    private boolean simulada;

    @Column(name = "medido_en")
    private Instant medidoEn;

    @Column(name = "caudal_lps", precision = 17, scale = 4)
    private BigDecimal caudalLitros;

    @Column(name = "umbral_lps", precision = 14, scale = 4)
    private BigDecimal umbralLitros;

    @Column(name = "riego_en")
    private Instant riegoEn;

    @Column(length = 500)
    private String nota;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public Mensaje(User autor, TipoMensaje tipo, AlcanceMensaje alcance, JuntaRegante juntaRegante,
                   Region region, Instant riegoEn, String nota, Instant creadoEn) {
        this.autor = autor;
        this.tipo = tipo;
        this.alcance = alcance;
        this.juntaRegante = juntaRegante;
        this.region = region;
        this.riegoEn = riegoEn;
        this.nota = nota;
        this.creadoEn = creadoEn;
    }

    public static Mensaje alertaCaudal(Dispositivo canal, TipoMensaje tipo, BigDecimal litros, BigDecimal umbral,
                                       Instant medidoEn, boolean simulada, String nota) {
        Mensaje mensaje = new Mensaje(null, tipo, AlcanceMensaje.CANAL,
                null, null, null, nota, Instant.now());
        mensaje.canal = canal;
        mensaje.caudalLitros = litros;
        mensaje.umbralLitros = umbral;
        mensaje.medidoEn = medidoEn;
        mensaje.simulada = simulada;
        return mensaje;
    }
}
