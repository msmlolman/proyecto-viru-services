package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.SignUpFarmerRequestDTO;
import com.MonitoreoViru.dto.UserResponseDTO;
import com.MonitoreoViru.mapper.UserMapper;
import com.MonitoreoViru.models.Agricultor;
import com.MonitoreoViru.models.JuntaRegante;
import com.MonitoreoViru.models.Role;
import com.MonitoreoViru.models.User;
import com.MonitoreoViru.repository.AgricultorRepository;
import com.MonitoreoViru.repository.JuntaReganteRepository;
import com.MonitoreoViru.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AgricultorService {

    private final UserRepository userRepository;
    private final AgricultorRepository agricultorRepository;
    private final JuntaReganteRepository juntaReganteRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AgricultorService(UserRepository userRepository,
                             AgricultorRepository agricultorRepository,
                             JuntaReganteRepository juntaReganteRepository,
                             UserMapper userMapper,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.agricultorRepository = agricultorRepository;
        this.juntaReganteRepository = juntaReganteRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO registrarAgricultor(SignUpFarmerRequestDTO req) {
        if (userRepository.existsByCorreo(req.correo().trim().toLowerCase())) {
            throw new IllegalStateException("El correo ya está registrado.");
        }

        JuntaRegante junta = juntaReganteRepository.findById(req.juntaReganteId())
                .orElseThrow(() -> new IllegalArgumentException("Junta regante no encontrada"));

        User user = new User(req.nombre(), req.correo(),
                passwordEncoder.encode(req.password()), Role.FARMER);

        Agricultor agricultor = new Agricultor(junta, user, req.apellido());
        agricultorRepository.save(agricultor); // cascade saves the User too

        return userMapper.toResponseDTO(agricultor.getUser());
    }

}
