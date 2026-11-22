package com.MonitoreoViru.repository;

import com.MonitoreoViru.models.Entrega;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    /** A farmer's inbox, newest first. */
    @Query("""
            select e from Entrega e join fetch e.mensaje m
            where e.agricultor.id = :agricultorId
              and (:soloNoLeidas = false or e.leidoEn is null)
            order by m.creadoEn desc
            """)
    List<Entrega> bandeja(@Param("agricultorId") Long agricultorId,
                          @Param("soloNoLeidas") boolean soloNoLeidas,
                          Pageable pageable);

    /** Scoped by farmer so nobody can mark someone else's message as read. */
    Optional<Entrega> findByIdAndAgricultorId(Long id, Long agricultorId);
}
