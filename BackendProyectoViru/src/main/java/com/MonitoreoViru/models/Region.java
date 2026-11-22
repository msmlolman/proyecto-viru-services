package com.MonitoreoViru.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ASSUMPTION: the table is called "region" with columns "id" and "nombre".
 * Adjust @Table / @Column to your real schema (ddl-auto=validate will tell you if it differs).
 */
@Entity
@Table(name = "region")
@Getter
@NoArgsConstructor
public class Region {

    @Id
    private Integer id;

    @Column
    private String nombre;
}
