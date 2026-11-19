package com.MonitoreoViru.services;

import com.MonitoreoViru.config.ViruProperties;
import com.MonitoreoViru.dto.SignUpAdminRequestDTO;
import com.MonitoreoViru.dto.UserResponseDTO;
import com.MonitoreoViru.mapper.UserMapper;
import com.MonitoreoViru.models.Administrador;
import com.MonitoreoViru.models.Role;
import com.MonitoreoViru.models.User;
import com.MonitoreoViru.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ViruProperties props;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    // Inyección por constructor (mejor práctica que @Autowired)
    public UserService(UserRepository userRepository, ViruProperties props, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.props = props;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User crearUsuario(String nombre, String correo, String password, Role role) {
        User nuevoUser = new User(nombre, correo, password, role);
        return userRepository.save(nuevoUser);
    }

    @Transactional
    public UserResponseDTO registrarAdministrador(SignUpAdminRequestDTO req) {
        boolean valid = MessageDigest.isEqual(
                props.adminCode().getBytes(StandardCharsets.UTF_8),
                req.codigoSecreto().getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            throw new IllegalArgumentException("Código de administrador inválido.");
        }
        User nuevoUser = crearUsuario(req.nombre(), req.correo(),  passwordEncoder.encode(req.password()), Role.ADMIN);

        return userMapper.toResponseDTO(nuevoUser);
    }
}