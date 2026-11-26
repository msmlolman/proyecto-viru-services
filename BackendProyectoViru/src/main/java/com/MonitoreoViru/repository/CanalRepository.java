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
                        BigDecimal umbralBajo, BigDecimal retornoBajo, boolean enAlerta,
                        boolean enAlertaBaja, Instant ultimaMedicion) {

        public boolean alto(BigDecimal caudal) {
            return enAlerta ? caudal.compareTo(retorno) > 0 : caudal.compareTo(umbralAlto) >= 0;
        }

        public boolean bajo(BigDecimal caudal) {
            return umbralBajo != null && (enAlertaBaja
                    ? caudal.compareTo(retornoBajo) < 0 : caudal.compareTo(umbralBajo) <= 0);
        }
    }

    private static final RowMapper<Regla> REGLA = (rs, row) -> {
        OffsetDateTime ultima = rs.getObject("ultima_medicion", OffsetDateTime.class);
        return new Regla(new ClaveRegla(rs.getLong("canal_id"), rs.getBoolean("simulada")),
                rs.getBigDecimal("umbral_alto_lps"), rs.getBigDecimal("retorno_lps"),
                rs.getBigDecimal("umbral_bajo_lps"), rs.getBigDecimal("retorno_bajo_lps"),
                rs.getBoolean("en_alerta"), rs.getBoolean("en_alerta_baja"),
                ultima == null ? null : ultima.toInstant());
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

    public List<Long> idsDeComision(Integer juntaId) {
        return jdbc.sql("""
                SELECT DISTINCT pc.canal_id
                FROM agricultor a JOIN predio p ON p.agricultor_id = a.id
                JOIN predio_canal pc ON pc.predio_id = p.id
                WHERE a.junta_regante_id = :juntaId
                """).param("juntaId", juntaId).query(Long.class).list();
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

    public void registrarEstado(ClaveRegla clave, boolean alto, boolean bajo, Instant medidoEn) {
        jdbc.sql("""
                UPDATE regla_caudal
                SET en_alerta = :alto, en_alerta_baja = :bajo, ultima_medicion = :medidoEn
                WHERE canal_id = :id AND simulada = :simulada
                """).param("alto", alto).param("bajo", bajo).param("medidoEn", medidoEn.atOffset(ZoneOffset.UTC))
                .param("id", clave.canalId()).param("simulada", clave.simulada()).update();
    }

    public String nombre(Long id) {
        return jdbc.sql("SELECT nombre FROM canal WHERE dispositivo_id = :id")
                .param("id", id).query(String.class).single();
    }
}
