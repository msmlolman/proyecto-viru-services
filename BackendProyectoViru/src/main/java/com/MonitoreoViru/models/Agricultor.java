package com.MonitoreoViru.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@NoArgsConstructor
public class Agricultor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String apellido;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "junta_regante_id")
    private JuntaRegante juntaRegante;

    public Agricultor(JuntaRegante juntaRegante, User user, String apellido) {
        this.juntaRegante = juntaRegante;
        this.user = user;
        this.apellido = apellido;
    }
}
