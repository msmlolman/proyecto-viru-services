package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.EnviarMensajeRequestDTO;
import com.MonitoreoViru.dto.MensajeEnviadoDTO;
import com.MonitoreoViru.exceptions.RecursoNoEncontradoException;
import com.MonitoreoViru.models.AlcanceMensaje;
import com.MonitoreoViru.models.Agricultor;
import com.MonitoreoViru.models.Entrega;
import com.MonitoreoViru.models.JuntaRegante;
import com.MonitoreoViru.models.Mensaje;
import com.MonitoreoViru.models.Region;
import com.MonitoreoViru.models.TipoMensaje;
import com.MonitoreoViru.models.User;
import com.MonitoreoViru.repository.AgricultorRepository;
import com.MonitoreoViru.repository.EntregaRepository;
import com.MonitoreoViru.repository.JuntaReganteRepository;
import com.MonitoreoViru.repository.MensajeRepository;
import com.MonitoreoViru.repository.RegionRepository;
import com.MonitoreoViru.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Admin side: write one message, deliver it to every farmer of a junta or a region. */
@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final EntregaRepository entregaRepository;
    private final AgricultorRepository agricultorRepository;
    private final JuntaReganteRepository juntaReganteRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;

    public MensajeService(MensajeRepository mensajeRepository,
                          EntregaRepository entregaRepository,
                          AgricultorRepository agricultorRepository,
                          JuntaReganteRepository juntaReganteRepository,
                          RegionRepository regionRepository,
                          UserRepository userRepository) {
        this.mensajeRepository = mensajeRepository;
        this.entregaRepository = entregaRepository;
        this.agricultorRepository = agricultorRepository;
        this.juntaReganteRepository = juntaReganteRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MensajeEnviadoDTO enviar(Long autorUserId, EnviarMensajeRequestDTO req) {
        validarCoherencia(req);

        JuntaRegante junta = null;
        Region region = null;
        List<Agricultor> destinatarios;

        if (req.alcance() == AlcanceMensaje.JUNTA) {
            junta = juntaReganteRepository.findById(req.juntaReganteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Junta regante no encontrada"));
            destinatarios = agricultorRepository.findByJuntaRegante_Id(junta.getId());
        } else {
            region = regionRepository.findById(req.regionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Región no encontrada"));
            destinatarios = agricultorRepository.findByJuntaRegante_Region_Id(region.getId());
        }

        if (destinatarios.isEmpty()) {
            throw new IllegalStateException("No hay agricultores en el destino seleccionado.");
        }

        User autor = userRepository.getReferenceById(autorUserId);
        Mensaje mensaje = mensajeRepository.save(new Mensaje(
                autor, req.tipo(), req.alcance(), junta, region,
                req.riegoEn(), req.nota(), Instant.now()));

        entregaRepository.saveAll(destinatarios.stream()
                .map(a -> new Entrega(mensaje, a))
                .toList());

        return new MensajeEnviadoDTO(
                mensaje.getId(), mensaje.getTipo(), mensaje.getAlcance(),
                destinatarios.size(), mensaje.getCreadoEn());
    }

    private void validarCoherencia(EnviarMensajeRequestDTO req) {
        if (req.tipo() == TipoMensaje.HORA_RIEGO) {
            if (req.riegoEn() == null) {
                throw new IllegalArgumentException("riegoEn es obligatorio para HORA_RIEGO.");
            }
        } else if (req.riegoEn() != null) {
            throw new IllegalArgumentException("riegoEn solo se permite en mensajes HORA_RIEGO.");
        }

        if (req.alcance() == AlcanceMensaje.JUNTA) {
            if (req.juntaReganteId() == null || req.regionId() != null) {
                throw new IllegalArgumentException("Alcance JUNTA requiere juntaReganteId y no regionId.");
            }
        } else {
            if (req.regionId() == null || req.juntaReganteId() != null) {
                throw new IllegalArgumentException("Alcance REGION requiere regionId y no juntaReganteId.");
            }
        }
    }
}
