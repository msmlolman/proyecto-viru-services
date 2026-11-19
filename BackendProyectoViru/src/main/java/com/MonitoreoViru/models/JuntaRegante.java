package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Data;

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
}
