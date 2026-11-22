package com.MonitoreoViru.mapper;

import com.MonitoreoViru.dto.UserResponseDTO;
import com.MonitoreoViru.models.User;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserResponseDTO toResponseDTO(User user);
}
