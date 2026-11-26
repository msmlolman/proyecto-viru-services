package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.Agricultor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {

    /** The JWT carries the user id; this finds that user's farmer profile. */
    Optional<Agricultor> findByUser_Id(Long userId);

    /** Recipients of a message with alcance JUNTA. */
    List<Agricultor> findByJuntaRegante_Id(Integer juntaId);

    /** Recipients of a message with alcance REGION (every junta in that region). */
    List<Agricultor> findByJuntaRegante_Region_Id(Integer regionId);

    @Query(value = """
            SELECT a.* FROM agricultor a
            WHERE EXISTS (
                SELECT 1 FROM predio p JOIN predio_canal pc ON pc.predio_id = p.id
                WHERE p.agricultor_id = a.id AND pc.canal_id = :canalId
            )
            ORDER BY a.id
            """, nativeQuery = true)
    List<Agricultor> destinatariosDeCanal(Long canalId);
}
