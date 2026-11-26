package com.MonitoreoViru.dto;

import com.MonitoreoViru.repository.CanalRepository.Regla;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public record EstadoCaudalDTO(BigDecimal litrosPorSegundo, BigDecimal umbralAlto, Boolean alto,
                              BigDecimal umbralBajo, Boolean bajo) {

    public static EstadoCaudalDTO de(BigDecimal litros, Regla regla) {
        return new EstadoCaudalDTO(litros, regla == null ? null : regla.umbralAlto(),
                regla == null || litros == null ? null : regla.alto(litros),
                regla == null ? null : regla.umbralBajo(),
                regla == null || litros == null || regla.umbralBajo() == null ? null : regla.bajo(litros));
    }

    public static Optional<BigDecimal> litros(List<VariableDTO> variables) {
        return variables.stream()
                .filter(v -> "caudal".equalsIgnoreCase(v.nombre()) && v.valor().signum() >= 0
                        && v.unidad() != null)
                .flatMap(v -> {
                    BigDecimal valor = v.valor().setScale(4, RoundingMode.HALF_UP);
                    return switch (v.unidad().trim().toLowerCase(Locale.ROOT)) {
                        case "l/s" -> Stream.of(valor);
                        case "m3/s", "m³/s" -> Stream.of(valor.movePointRight(3));
                        default -> Stream.empty();
                    };
                }).findFirst();
    }
}
