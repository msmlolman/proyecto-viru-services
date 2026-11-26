package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.EstadoTransmision;
import com.MonitoreoViru.models.Transmision;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransmisionRepository extends JpaRepository<Transmision, Long> {

    /** Bloquea la ranura pendiente hasta que termina la ingesta. */
    @Query(value = """
            SELECT * FROM transaccion
            WHERE dispositivo_id = :dispositivoId AND estado = 'PENDIENTE'
            ORDER BY esperado_en LIMIT 1 FOR NO KEY UPDATE
            """, nativeQuery = true)
    Optional<Transmision> primeraPendiente(@Param("dispositivoId") Long dispositivoId);

    /** Slots whose deadline passed with nothing received. Locked for the same reason. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transmision t where t.estado = :estado and t.limiteEn < :ahora")
    List<Transmision> vencidas(@Param("estado") EstadoTransmision estado,
                               @Param("ahora") Instant ahora);

    /** Per active device: its most recent finished (non-PENDIENTE) slot. Drives the signal status. */
    @Query("""
            select t from Transmision t join fetch t.dispositivo d
            where d.id in :ids
              and t.estado <> :pendiente
              and t.esperadoEn = (select max(t2.esperadoEn) from Transmision t2
                                  where t2.dispositivo = t.dispositivo
                                    and t2.estado <> :pendiente)
            """)
    List<Transmision> ultimaFinalizadaPorDispositivo(@Param("pendiente") EstadoTransmision pendiente,
                                                     @Param("ids") Collection<Long> ids);
}
