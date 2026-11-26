package com.MonitoreoViru.dto;

import java.time.Instant;
import java.util.List;

public record MuestraDTO(Instant medidoEn, Boolean simulada, List<VariableDTO> variables) {}
