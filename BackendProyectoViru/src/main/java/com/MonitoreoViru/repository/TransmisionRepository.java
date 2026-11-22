package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.EstadoTransmision;
import com.MonitoreoViru.models.Transmision;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransmisionRepository extends JpaRepository<Transmision, Long> {

    /** Slot an incoming message is matched to. Locked so the monitor job cannot flip it meanwhile. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transmision> findFirstByDispositivoIdAndEstadoOrderByEsperadoEnAsc(
            Long dispositivoId, EstadoTransmision estado);

    /** Slots whose deadline passed with nothing received. Locked for the same reason. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transmision t where t.estado = :estado and t.limiteEn < :ahora")
    List<Transmision> vencidas(@Param("estado") EstadoTransmision estado,
                               @Param("ahora") Instant ahora);

    /** Per active device: its most recent finished (non-PENDIENTE) slot. Drives the signal status. */
    @Query("""
            select t from Transmision t join fetch t.dispositivo d
            where d.activo = true
              and t.estado <> :pendiente
              and t.esperadoEn = (select max(t2.esperadoEn) from Transmision t2
                                  where t2.dispositivo = t.dispositivo
                                    and t2.estado <> :pendiente)
            """)
    List<Transmision> ultimaFinalizadaPorDispositivo(@Param("pendiente") EstadoTransmision pendiente);
}
