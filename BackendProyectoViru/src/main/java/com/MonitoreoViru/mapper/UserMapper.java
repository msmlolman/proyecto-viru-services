package com.MonitoreoViru.mapper;

import com.MonitoreoViru.dto.UserResponseDTO;
import com.MonitoreoViru.mapper.MapStructConfig;
import com.MonitoreoViru.dto.SignUpAdminRequestDTO;
import com.MonitoreoViru.dto.SignUpFarmerRequestDTO;
import com.MonitoreoViru.models.JuntaRegante;
import com.MonitoreoViru.models.Role;
import com.MonitoreoViru.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserResponseDTO toResponseDTO(User user);

    // 1. Para Administrador (Ignoramos la junta, asignamos rol ADMIN)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "ADMIN") // MapStruct pondrá Role.ADMIN automáticamente
    @Mapping(target = "juntaRegante", ignore = true) // No tiene junta
    User toAdminEntity(SignUpAdminRequestDTO request);

    // 2. Para Agricultor (Le pasamos la Junta que ya buscamos en el servicio)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "FARMER")
    @Mapping(target = "juntaRegante", ignore = true) // Toma la junta del parámetro
    User toFarmerEntity(SignUpFarmerRequestDTO request);


}
