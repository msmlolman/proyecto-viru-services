package com.MonitoreoViru.controllers;

import com.MonitoreoViru.dto.*;
import com.MonitoreoViru.services.AgricultorService;
import com.MonitoreoViru.services.AuthService;
import com.MonitoreoViru.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final UserService userService;
    private final AgricultorService agricultorService;
    private final AuthService authService;

    public AuthController(UserService userService, AgricultorService agricultorService, AuthService authService) {
        this.userService = userService;
        this.agricultorService = agricultorService;
        this.authService = authService;
    }

    @PostMapping("/register/admin")
    public ResponseEntity<UserResponseDTO> registrarAdmin(
            @Valid @RequestBody SignUpAdminRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registrarAdministrador(request));
    }

    @PostMapping("/register/farmer")
    public ResponseEntity<UserResponseDTO> registrarAgricultor(
            @Valid @RequestBody SignUpFarmerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agricultorService.registrarAgricultor(request));
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO req) {
        return authService.login(req);
    }

}
