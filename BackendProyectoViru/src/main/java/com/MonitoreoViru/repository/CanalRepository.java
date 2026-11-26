package com.MonitoreoViru.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CanalRepository {

    public record Canal(Long id, String codigo, String nombre) {}

    public record ClaveRegla(Long canalId, boolean simulada) {}

    public record Regla(ClaveRegla clave, BigDecimal umbralAlto, BigDecimal retorno,
                        boolean enAlerta, Instant ultimaMedicion) {}

    private static final RowMapper<Regla> REGLA = (rs, row) -> {
        OffsetDateTime ultima = rs.getObject("ultima_medicion", OffsetDateTime.class);
        return new Regla(new ClaveRegla(rs.getLong("canal_id"), rs.getBoolean("simulada")),
                rs.getBigDecimal("umbral_alto_lps"), rs.getBigDecimal("retorno_lps"),
                rs.getBoolean("en_alerta"), ultima == null ? null : ultima.toInstant());
    };

    private final JdbcClient jdbc;

    public CanalRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Canal> activos() {
        return jdbc.sql("""
                SELECT d.id, d.codigo, c.nombre
                FROM canal c JOIN dispositivo d ON d.id = c.dispositivo_id
                WHERE d.activo ORDER BY d.codigo
                """).query(Canal.class).list();
    }

    public Map<ClaveRegla, Regla> reglas(Collection<Long> ids) {
        return jdbc.sql("SELECT * FROM regla_caudal WHERE canal_id IN (:ids)")
                .param("ids", ids).query(REGLA).list().stream()
                .collect(Collectors.toMap(Regla::clave, Function.identity()));
    }

    public Optional<Regla> bloquearRegla(Long canalId, boolean simulada) {
        return jdbc.sql("""
                SELECT * FROM regla_caudal
                WHERE canal_id = :id AND simulada = :simulada FOR UPDATE
                """).param("id", canalId).param("simulada", simulada).query(REGLA).optional();
    }

    public void registrarEstado(ClaveRegla clave, boolean alto, Instant medidoEn) {
        jdbc.sql("""
                UPDATE regla_caudal SET en_alerta = :alto, ultima_medicion = :medidoEn
                WHERE canal_id = :id AND simulada = :simulada
                """).param("alto", alto).param("medidoEn", medidoEn.atOffset(ZoneOffset.UTC))
                .param("id", clave.canalId()).param("simulada", clave.simulada()).update();
    }

    public String nombre(Long id) {
        return jdbc.sql("SELECT nombre FROM canal WHERE dispositivo_id = :id")
                .param("id", id).query(String.class).single();
    }
}
