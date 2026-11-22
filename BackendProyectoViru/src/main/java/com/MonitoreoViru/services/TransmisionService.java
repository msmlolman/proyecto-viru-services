package com.MonitoreoViru.services;

import com.MonitoreoViru.models.Dispositivo;
import com.MonitoreoViru.models.EstadoTransmision;
import com.MonitoreoViru.models.Transmision;
import com.MonitoreoViru.repository.DispositivoRepository;
import com.MonitoreoViru.repository.TransmisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TransmisionService {

    private final TransmisionRepository transmisionRepository;
    private final DispositivoRepository dispositivoRepository;

    public TransmisionService(TransmisionRepository transmisionRepository,
                              DispositivoRepository dispositivoRepository) {
        this.transmisionRepository = transmisionRepository;
        this.dispositivoRepository = dispositivoRepository;
    }

    /**
     * Marks overdue slots as PERDIDA and keeps every active device supplied with a pending slot.
     * Called by TransmisionMonitorJob. Returns how many slots were marked lost.
     */
    @Transactional
    public int marcarPerdidas() {
        Instant ahora = Instant.now();

        List<Transmision> vencidas = transmisionRepository.vencidas(EstadoTransmision.PENDIENTE, ahora);
        for (Transmision slot : vencidas) {
            slot.marcarPerdida();

            Dispositivo d = slot.getDispositivo();
            if (!d.isActivo()) {
                continue;
            }
            Instant siguiente = slot.getEsperadoEn().plusSeconds(d.getIntervaloSegundos());
            boolean siguienteYaVencido = !siguiente.plusSeconds(d.getToleranciaSegundos()).isAfter(ahora);
            if (siguienteYaVencido) {
                // The job was down for a while: do not back-fill, restart the schedule from now
                siguiente = ahora.plusSeconds(d.getIntervaloSegundos());
            }
            transmisionRepository.save(Transmision.pendiente(d, siguiente));
        }

        // New devices, or devices whose chain was broken
        for (Dispositivo d : dispositivoRepository.sinRanuraPendiente(EstadoTransmision.PENDIENTE)) {
            transmisionRepository.save(
                    Transmision.pendiente(d, ahora.plusSeconds(d.getIntervaloSegundos())));
        }

        return vencidas.size();
    }
}
