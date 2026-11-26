package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.DispositivoEstadoDTO;
import com.MonitoreoViru.exceptions.RecursoNoEncontradoException;
import com.MonitoreoViru.repository.AgricultorRepository;
import com.MonitoreoViru.repository.CanalRepository;
import com.MonitoreoViru.repository.CanalRepository.Canal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PredioService {

    public record Asignacion(@NotNull @Positive Long agricultorId, @NotBlank @Size(max = 120) String nombre,
                             @NotEmpty @Size(max = 20) List<@NotNull @Positive Long> canalIds) {}
    public record AgricultorItem(Long id, String nombre, String correo, String junta) {}
    public record Predio(Long id, String codigo, String nombre, Long agricultorId, List<Canal> canales) {}
    public record PredioFila(Long id, String codigo, String nombre, Long agricultorId,
                             Long canalId, String canalCodigo, String canalNombre) {}
    public record Panel(List<AgricultorItem> agricultores, List<Canal> canales, List<Predio> predios) {}
    public record MisPredios(List<Predio> predios, List<DispositivoEstadoDTO> canales) {}
    public record RegionItem(Integer id, String nombre) {}
    public record JuntaItem(Integer id, String nombre, Integer regionId) {}
    public record Catalogo(List<RegionItem> regiones, List<JuntaItem> juntas) {}

    private final JdbcClient jdbc;
    private final CanalRepository canales;
    private final AgricultorRepository agricultores;
    private final MonitoreoService monitoreo;

    public PredioService(JdbcClient jdbc, CanalRepository canales, AgricultorRepository agricultores,
                          MonitoreoService monitoreo) {
        this.jdbc = jdbc;
        this.canales = canales;
        this.agricultores = agricultores;
        this.monitoreo = monitoreo;
    }

    @Transactional(readOnly = true)
    public Catalogo catalogo() {
        return new Catalogo(
                jdbc.sql("SELECT id, nombre FROM region ORDER BY nombre").query(RegionItem.class).list(),
                jdbc.sql("SELECT id, nombre, region_id FROM junta_regante ORDER BY nombre").query(JuntaItem.class).list());
    }

    @Transactional(readOnly = true)
    public Panel panel() {
        var lista = jdbc.sql("""
                SELECT a.id, concat_ws(' ', u.nombre, a.apellido) AS nombre, u.correo, j.nombre AS junta
                FROM agricultor a JOIN usuario u ON u.id = a.user_id
                JOIN junta_regante j ON j.id = a.junta_regante_id
                ORDER BY nombre, a.id
                """).query(AgricultorItem.class).list();
        return new Panel(lista, canales.activos(), predios(null));
    }

    @Transactional(readOnly = true)
    public MisPredios misPredios(Long usuarioId) {
        Long agricultorId = agricultores.findByUser_Id(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de agricultor no encontrado")).getId();
        var predios = predios(agricultorId);
        var ids = predios.stream().flatMap(p -> p.canales().stream()).map(Canal::id).distinct().toList();
        return new MisPredios(predios, monitoreo.estadoCanales(ids));
    }

    @Transactional
    public void guardar(String codigo, Asignacion asignacion) {
        if (!codigo.matches("[A-Z0-9][A-Z0-9-]{0,31}")) {
            throw new IllegalArgumentException("Usa un código de predio con mayúsculas, números y guiones.");
        }
        if (!agricultores.existsById(asignacion.agricultorId())) {
            throw new RecursoNoEncontradoException("Agricultor no encontrado");
        }
        var ids = new HashSet<>(asignacion.canalIds());
        var activos = canales.activos().stream().map(Canal::id).collect(Collectors.toSet());
        if (!activos.containsAll(ids)) {
            throw new IllegalArgumentException("Selecciona canales activos del catálogo.");
        }
        Long predio = jdbc.sql("""
                INSERT INTO predio (agricultor_id, codigo, nombre) VALUES (:agricultor, :codigo, :nombre)
                ON CONFLICT (codigo) DO UPDATE SET agricultor_id = excluded.agricultor_id, nombre = excluded.nombre
                RETURNING id
                """).param("agricultor", asignacion.agricultorId()).param("codigo", codigo)
                .param("nombre", asignacion.nombre().strip()).query(Long.class).single();
        jdbc.sql("DELETE FROM predio_canal WHERE predio_id = :id").param("id", predio).update();
        jdbc.sql("""
                INSERT INTO predio_canal (predio_id, canal_id)
                SELECT :predio, dispositivo_id FROM canal WHERE dispositivo_id IN (:canales)
                """).param("predio", predio).param("canales", ids).update();
    }

    private List<Predio> predios(Long agricultorId) {
        var filas = jdbc.sql("""
                SELECT p.id, p.codigo, p.nombre, p.agricultor_id, c.dispositivo_id AS canal_id,
                       d.codigo AS canal_codigo, c.nombre AS canal_nombre
                FROM predio p
                LEFT JOIN predio_canal pc ON pc.predio_id = p.id
                LEFT JOIN canal c ON c.dispositivo_id = pc.canal_id
                LEFT JOIN dispositivo d ON d.id = c.dispositivo_id
                WHERE (CAST(:agricultor AS bigint) IS NULL OR p.agricultor_id = :agricultor)
                ORDER BY p.codigo, d.codigo
                """).param("agricultor", agricultorId, java.sql.Types.BIGINT).query(PredioFila.class).list();
        return filas.stream().collect(Collectors.groupingBy(PredioFila::id, LinkedHashMap::new, Collectors.toList()))
                .values().stream().map(items -> {
                    var p = items.get(0);
                    var asignados = items.stream().filter(i -> i.canalId() != null)
                            .map(i -> new Canal(i.canalId(), i.canalCodigo(), i.canalNombre())).toList();
                    return new Predio(p.id(), p.codigo(), p.nombre(), p.agricultorId(), asignados);
                }).toList();
    }
}
