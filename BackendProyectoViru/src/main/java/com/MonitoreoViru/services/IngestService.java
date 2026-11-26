package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.IngestRequestDTO;
import com.MonitoreoViru.dto.LecturaEnVivoDTO;
import com.MonitoreoViru.exceptions.RecursoNoEncontradoException;
import com.MonitoreoViru.models.Dispositivo;
import com.MonitoreoViru.models.Lectura;
import com.MonitoreoViru.models.Transmision;
import com.MonitoreoViru.repository.DispositivoRepository;
import com.MonitoreoViru.repository.LecturaRepository;
import com.MonitoreoViru.repository.TransmisionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Handles one incoming transaction from a device:
 * find the device, close its pending slot, store the readings, open the next slot,
 * and tell the live stream once the transaction has committed.
 */
@Service
public class IngestService {

    private final DispositivoRepository dispositivoRepository;
    private final TransmisionRepository transmisionRepository;
    private final LecturaRepository lecturaRepository;
    private final ApplicationEventPublisher events;
    private final AlertaCaudalService alertas;
    private final JdbcClient jdbc;

    public IngestService(DispositivoRepository dispositivoRepository,
                         TransmisionRepository transmisionRepository,
                         LecturaRepository lecturaRepository,
                         ApplicationEventPublisher events,
                         AlertaCaudalService alertas,
                         JdbcClient jdbc) {
        this.dispositivoRepository = dispositivoRepository;
        this.transmisionRepository = transmisionRepository;
        this.lecturaRepository = lecturaRepository;
        this.events = events;
        this.alertas = alertas;
        this.jdbc = jdbc;
    }

    @Transactional
    public void registrar(IngestRequestDTO req) {
        Instant ahora = Instant.now();

        Dispositivo dispositivo = dispositivoRepository
                .findByDevEuiIgnoreCaseAndActivoTrue(req.devEui().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Dispositivo no registrado o inactivo: " + req.devEui()));

        // Serializa reentregas simultáneas durante la transacción sin actualizar el catálogo.
        jdbc.sql("SELECT pg_advisory_xact_lock(hashtextextended(:clave, 0))")
                .param("clave", "viru.ingesta:" + dispositivo.getId()).query().listOfRows();

        // Network-server timestamp if provided; never trust the device clock for this
        Instant recibidoNube = req.recibidoEn() != null ? req.recibidoEn() : ahora;
        Instant medidoEn = (req.medidoEn() != null ? req.medidoEn() : recibidoNube)
                .truncatedTo(ChronoUnit.MICROS);
        boolean simulada = Boolean.TRUE.equals(req.simulada());

        // The same message delivered twice (webhook retry): acknowledge without storing again
        if (lecturaRepository.existsByDispositivoIdAndMedidoEn(dispositivo.getId(), medidoEn)) {
            return;
        }

        // Oldest pending slot; if the device never had one, record this arrival as its own slot
        Transmision slot = transmisionRepository
                .primeraPendiente(dispositivo.getId())
                .orElseGet(() -> transmisionRepository.save(
                        Transmision.pendiente(dispositivo, recibidoNube)));

        List<Lectura> lecturas = req.variables().stream()
                .map(v -> new Lectura(slot, dispositivo, v.nombre(), v.valor(), v.unidad(), medidoEn, simulada))
                .toList();
        lecturaRepository.saveAll(lecturas);

        slot.registrarRecepcion(recibidoNube, Instant.now());

        // Next check-in is expected one interval after this arrival (keeps the schedule aligned)
        transmisionRepository.save(Transmision.pendiente(
                dispositivo, recibidoNube.plusSeconds(dispositivo.getIntervaloSegundos())));

        var caudal = alertas.evaluar(dispositivo, req.variables(), medidoEn, simulada);

        // Delivered to LiveStreamService only after this transaction commits
        events.publishEvent(new LecturaEnVivoDTO(
                dispositivo.getId(),
                dispositivo.getCodigo(),
                dispositivo.getTipo(),
                medidoEn,
                slot.getEstado(),
                req.variables(),
                simulada,
                caudal));
    }
}
