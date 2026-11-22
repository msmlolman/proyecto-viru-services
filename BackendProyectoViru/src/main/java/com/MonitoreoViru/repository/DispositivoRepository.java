package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.Dispositivo;
import com.MonitoreoViru.models.EstadoTransmision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {

    Optional<Dispositivo> findByDevEuiIgnoreCaseAndActivoTrue(String devEui);

    List<Dispositivo> findByActivoTrueOrderByCodigoAsc();

    /** Active devices that currently have no PENDIENTE slot (new device, or first run). */
    @Query("""
            select d from Dispositivo d
            where d.activo = true
              and not exists (select t.id from Transmision t
                              where t.dispositivo = d and t.estado = :pendiente)
            """)
    List<Dispositivo> sinRanuraPendiente(@Param("pendiente") EstadoTransmision pendiente);
}
