package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.EstadoCaudalDTO;
import com.MonitoreoViru.dto.VariableDTO;
import com.MonitoreoViru.models.Dispositivo;
import com.MonitoreoViru.models.TipoDispositivo;
import com.MonitoreoViru.models.TipoMensaje;
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
                                   Instant medidoEn, boolean simulada, Instant recibidoEn) {
        BigDecimal caudal = EstadoCaudalDTO.litros(variables).orElse(null);
        if (caudal == null || dispositivo.getTipo() != TipoDispositivo.CAUDALIMETRO_CANAL) {
            return EstadoCaudalDTO.de(caudal, null);
        }
        if (dispositivo.vigenciaDe(medidoEn).isBefore(recibidoEn)) {
            return EstadoCaudalDTO.de(caudal, null);
        }
        Regla regla = canales.bloquearRegla(dispositivo.getId(), simulada).orElse(null);
        var estado = EstadoCaudalDTO.de(caudal, regla);
        if (regla == null || (regla.ultimaMedicion() != null && !medidoEn.isAfter(regla.ultimaMedicion()))) {
            return estado;
        }

        boolean alto = Boolean.TRUE.equals(estado.alto());
        boolean bajo = Boolean.TRUE.equals(estado.bajo());
        if (alto && !regla.enAlerta()) {
            mensajes.alertaCaudal(dispositivo, canales.nombre(dispositivo.getId()), TipoMensaje.ALERTA_ALTO_CAUDAL, caudal,
                    regla.umbralAlto(), medidoEn, simulada);
        } else if (bajo && !regla.enAlertaBaja()) {
            mensajes.alertaCaudal(dispositivo, canales.nombre(dispositivo.getId()), TipoMensaje.ALERTA_BAJO_CAUDAL, caudal,
                    regla.umbralBajo(), medidoEn, simulada);
        }
        canales.registrarEstado(regla.clave(), alto, bajo, medidoEn);
        return estado;
    }
}
