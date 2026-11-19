package com.MonitoreoViru.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        componentModel = "spring", // Para que Spring lo inyecte como un Bean
        unmappedTargetPolicy = ReportingPolicy.ERROR // Te avisa si olvidas mapear un campo
)
public interface MapStructConfig {
}