package com.MonitoreoViru.services;

import com.MonitoreoViru.config.ViruProperties;
import com.MonitoreoViru.dto.LoginRequestDTO;
import com.MonitoreoViru.dto.LoginResponseDTO;
import com.MonitoreoViru.mapper.UserMapper;
import com.MonitoreoViru.models.User;
import com.MonitoreoViru.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final ViruProperties props;
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, UserMapper userMapper, ViruProperties props) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.props = props;
        this.dummyHash = passwordEncoder.encode("dummy-password");
    }

    public LoginResponseDTO login(LoginRequestDTO req) {
        Optional<User> user = userRepository.findByCorreo(req.correo().trim().toLowerCase());

        // Always run one BCrypt check, so response time doesn't reveal whether the email exists
        String hash = user.map(User::getPassword).orElse(dummyHash);
        boolean ok = passwordEncoder.matches(req.password(), hash);

        if (user.isEmpty() || !ok) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return new LoginResponseDTO(
                jwtService.generarToken(user.get()),
                "Bearer",
                props.jwt().expirationMinutes() * 60,
                userMapper.toResponseDTO(user.get()));
    }
}