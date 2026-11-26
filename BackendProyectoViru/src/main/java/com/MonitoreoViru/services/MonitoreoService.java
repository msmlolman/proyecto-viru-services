package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.DispositivoEstadoDTO;
import com.MonitoreoViru.dto.EstadoCaudalDTO;
import com.MonitoreoViru.dto.MuestraDTO;
import com.MonitoreoViru.dto.VariableDTO;
import com.MonitoreoViru.models.Dispositivo;
import com.MonitoreoViru.models.EstadoSenal;
import com.MonitoreoViru.models.EstadoTransmision;
import com.MonitoreoViru.models.Lectura;
import com.MonitoreoViru.models.Transmision;
import com.MonitoreoViru.repository.CanalRepository;
import com.MonitoreoViru.repository.CanalRepository.Canal;
import com.MonitoreoViru.repository.CanalRepository.ClaveRegla;
import com.MonitoreoViru.repository.DispositivoRepository;
import com.MonitoreoViru.repository.LecturaRepository;
import com.MonitoreoViru.repository.TransmisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonitoreoService {

    private final DispositivoRepository dispositivoRepository;
    private final LecturaRepository lecturaRepository;
    private final TransmisionRepository transmisionRepository;
    private final CanalRepository canalRepository;

    public MonitoreoService(DispositivoRepository dispositivoRepository,
                            LecturaRepository lecturaRepository,
                            TransmisionRepository transmisionRepository,
                            CanalRepository canalRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.lecturaRepository = lecturaRepository;
        this.transmisionRepository = transmisionRepository;
        this.canalRepository = canalRepository;
    }

    /** Snapshot of every active device: latest values plus whether it is still reporting. */
    @Transactional(readOnly = true)
    public List<DispositivoEstadoDTO> estadoActual() {
        return estado(dispositivoRepository.findByActivoTrueOrderByCodigoAsc());
    }

    @Transactional(readOnly = true)
    public List<DispositivoEstadoDTO> estadoCanales(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : estado(dispositivoRepository.findByIdInAndActivoTrueOrderByCodigoAsc(ids));
    }

    private List<DispositivoEstadoDTO> estado(List<Dispositivo> dispositivos) {
        if (dispositivos.isEmpty()) {
            return List.of();
        }
        var ids = dispositivos.stream().map(Dispositivo::getId).toList();
        var reglas = canalRepository.reglas(ids);
        Map<Long, String> nombres = canalRepository.activos().stream()
                .collect(Collectors.toMap(Canal::id, Canal::nombre));
        Map<Long, List<Lectura>> ultimasLecturas = lecturaRepository.ultimasPorDispositivo(ids).stream()
                .collect(Collectors.groupingBy(l -> l.getDispositivo().getId()));

        Map<Long, EstadoTransmision> ultimoEstado = transmisionRepository
                .ultimaFinalizadaPorDispositivo(EstadoTransmision.PENDIENTE, ids).stream()
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
            Boolean simulada = lecturas.isEmpty() ? null : lecturas.get(0).getSimulada();
            List<VariableDTO> variables = variables(lecturas);
            var regla = simulada == null ? null : reglas.get(new ClaveRegla(d.getId(), simulada));
            var caudal = EstadoCaudalDTO.de(EstadoCaudalDTO.litros(variables).orElse(null), regla);

            return new DispositivoEstadoDTO(
                    d.getId(), d.getCodigo(), d.getTipo(), d.getModelo(), d.getUbicacion(),
                    senal, medidoEn, variables, nombres.get(d.getId()), simulada, caudal,
                    medidoEn == null ? null : d.vigenciaDe(medidoEn));
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<MuestraDTO> historial(Long id, int limite) {
        var porFecha = lecturaRepository.historial(id, Math.min(Math.max(limite, 1), 120)).stream()
                .collect(Collectors.groupingBy(Lectura::getMedidoEn, LinkedHashMap::new, Collectors.toList()));
        return porFecha.entrySet().stream()
                .map(e -> new MuestraDTO(e.getKey(), e.getValue().get(0).getSimulada(), variables(e.getValue())))
                .toList();
    }

    private List<VariableDTO> variables(List<Lectura> lecturas) {
        return lecturas.stream().map(l -> new VariableDTO(l.getVariable(), l.getValor(), l.getUnidad())).toList();
    }
}
