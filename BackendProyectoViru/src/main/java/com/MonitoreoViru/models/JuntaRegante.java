package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "junta_regante")
public class JuntaRegante {
    @Id
    private int id;

    @Column
    private String nombre;

    @Column(name = "creada_en")
    private LocalDateTime creadaEn;

    // NEW: region ("area") of this junta. The column region_id already exists in the database.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Region region;
}
