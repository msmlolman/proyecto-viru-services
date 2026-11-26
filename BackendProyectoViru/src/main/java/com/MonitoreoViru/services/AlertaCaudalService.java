package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.EstadoCaudalDTO;
import com.MonitoreoViru.dto.VariableDTO;
import com.MonitoreoViru.models.Dispositivo;
import com.MonitoreoViru.models.TipoDispositivo;
import com.MonitoreoViru.repository.CanalRepository;
import com.MonitoreoViru.repository.CanalRepository.Regla;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class AlertaCaudalService {

    private final CanalRepository canales;
    private final MensajeService mensajes;

    public AlertaCaudalService(CanalRepository canales, MensajeService mensajes) {
        this.canales = canales;
        this.mensajes = mensajes;
    }

    /** Se ejecuta en la misma transacción que la ingesta. */
    public EstadoCaudalDTO evaluar(Dispositivo dispositivo, List<VariableDTO> variables,
                                   Instant medidoEn, boolean simulada) {
        BigDecimal caudal = EstadoCaudalDTO.litros(variables).orElse(null);
        if (caudal == null || dispositivo.getTipo() != TipoDispositivo.CAUDALIMETRO_CANAL) {
            return EstadoCaudalDTO.de(caudal, null);
        }
        Regla regla = canales.bloquearRegla(dispositivo.getId(), simulada).orElse(null);
        if (regla == null || (regla.ultimaMedicion() != null && !medidoEn.isAfter(regla.ultimaMedicion()))) {
            return EstadoCaudalDTO.de(caudal, regla);
        }

        boolean alto = regla.enAlerta()
                ? caudal.compareTo(regla.retorno()) > 0
                : caudal.compareTo(regla.umbralAlto()) >= 0;
        if (alto && !regla.enAlerta()) {
            mensajes.alertaCaudal(dispositivo, canales.nombre(dispositivo.getId()), caudal,
                    regla.umbralAlto(), medidoEn, simulada);
        }
        canales.registrarEstado(regla.clave(), alto, medidoEn);
        return new EstadoCaudalDTO(caudal, regla.umbralAlto(), alto);
    }
}
