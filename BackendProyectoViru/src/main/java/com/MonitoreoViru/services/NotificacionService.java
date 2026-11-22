package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.NotificacionDTO;
import com.MonitoreoViru.exceptions.RecursoNoEncontradoException;
import com.MonitoreoViru.models.Agricultor;
import com.MonitoreoViru.models.Entrega;
import com.MonitoreoViru.repository.AgricultorRepository;
import com.MonitoreoViru.repository.EntregaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Farmer side: read the inbox and mark messages as read. The farmer always comes from the JWT. */
@Service
public class NotificacionService {

    private static final int MAX_LIMITE = 100;

    private final EntregaRepository entregaRepository;
    private final AgricultorRepository agricultorRepository;

    public NotificacionService(EntregaRepository entregaRepository,
                               AgricultorRepository agricultorRepository) {
        this.entregaRepository = entregaRepository;
        this.agricultorRepository = agricultorRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> bandeja(Long userId, boolean soloNoLeidas, int limite) {
        Agricultor agricultor = agricultorDelUsuario(userId);
        int size = Math.min(Math.max(limite, 1), MAX_LIMITE);
        return entregaRepository
                .bandeja(agricultor.getId(), soloNoLeidas, PageRequest.of(0, size))
                .stream()
                .map(NotificacionDTO::from)
                .toList();
    }

    @Transactional
    public void marcarLeida(Long userId, Long entregaId) {
        Agricultor agricultor = agricultorDelUsuario(userId);
        Entrega entrega = entregaRepository.findByIdAndAgricultorId(entregaId, agricultor.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Mensaje no encontrado"));
        entrega.marcarLeida(Instant.now());
    }

    private Agricultor agricultorDelUsuario(Long userId) {
        return agricultorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de agricultor no encontrado"));
    }
}
