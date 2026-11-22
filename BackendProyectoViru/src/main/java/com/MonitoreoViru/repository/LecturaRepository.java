package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    /** Used to ignore a webhook that is delivered twice. */
    boolean existsByDispositivoIdAndMedidoEn(Long dispositivoId, Instant medidoEn);

    /** All variables of the most recent measurement of every active device (one query). */
    @Query("""
            select l from Lectura l join fetch l.dispositivo d
            where d.activo = true
              and l.medidoEn = (select max(l2.medidoEn) from Lectura l2
                                where l2.dispositivo = l.dispositivo)
            """)
    List<Lectura> ultimasPorDispositivo();
}
