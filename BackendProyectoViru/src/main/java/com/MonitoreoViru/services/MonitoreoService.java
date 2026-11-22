package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.DispositivoEstadoDTO;
import com.MonitoreoViru.dto.VariableDTO;
import com.MonitoreoViru.models.Dispositivo;
import com.MonitoreoViru.models.EstadoSenal;
import com.MonitoreoViru.models.EstadoTransmision;
import com.MonitoreoViru.models.Lectura;
import com.MonitoreoViru.models.Transmision;
import com.MonitoreoViru.repository.DispositivoRepository;
import com.MonitoreoViru.repository.LecturaRepository;
import com.MonitoreoViru.repository.TransmisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonitoreoService {

    private final DispositivoRepository dispositivoRepository;
    private final LecturaRepository lecturaRepository;
    private final TransmisionRepository transmisionRepository;

    public MonitoreoService(DispositivoRepository dispositivoRepository,
                            LecturaRepository lecturaRepository,
                            TransmisionRepository transmisionRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.lecturaRepository = lecturaRepository;
        this.transmisionRepository = transmisionRepository;
    }

    /** Snapshot of every active device: latest values plus whether it is still reporting. */
    @Transactional(readOnly = true)
    public List<DispositivoEstadoDTO> estadoActual() {
        List<Dispositivo> dispositivos = dispositivoRepository.findByActivoTrueOrderByCodigoAsc();

        Map<Long, List<Lectura>> ultimasLecturas = lecturaRepository.ultimasPorDispositivo().stream()
                .collect(Collectors.groupingBy(l -> l.getDispositivo().getId()));

        Map<Long, EstadoTransmision> ultimoEstado = transmisionRepository
                .ultimaFinalizadaPorDispositivo(EstadoTransmision.PENDIENTE).stream()
                .collect(Collectors.toMap(
                        t -> t.getDispositivo().getId(),
                        Transmision::getEstado,
                        (a, b) -> a));

        return dispositivos.stream().map(d -> {
            List<Lectura> lecturas = ultimasLecturas.getOrDefault(d.getId(), List.of());
            EstadoTransmision estado = ultimoEstado.get(d.getId());

            EstadoSenal senal;
            if (estado == null) {
                senal = EstadoSenal.SIN_DATOS;
            } else if (estado == EstadoTransmision.PERDIDA) {
                senal = EstadoSenal.SIN_SENAL;
            } else {
                senal = EstadoSenal.EN_LINEA;
            }

            Instant medidoEn = lecturas.isEmpty() ? null : lecturas.get(0).getMedidoEn();
            List<VariableDTO> variables = lecturas.stream()
                    .map(l -> new VariableDTO(l.getVariable(), l.getValor(), l.getUnidad()))
                    .toList();

            return new DispositivoEstadoDTO(
                    d.getId(), d.getCodigo(), d.getTipo(), d.getModelo(), d.getUbicacion(),
                    senal, medidoEn, variables);
        }).toList();
    }
}
