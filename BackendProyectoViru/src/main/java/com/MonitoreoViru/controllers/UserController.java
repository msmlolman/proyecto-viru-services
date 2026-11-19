package com.MonitoreoViru.controllers;

import com.MonitoreoViru.dto.SignUpAdminRequestDTO;
import com.MonitoreoViru.dto.SignUpFarmerRequestDTO;
import com.MonitoreoViru.dto.UserResponseDTO;
import com.MonitoreoViru.services.AgricultorService;
import com.MonitoreoViru.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AgricultorService agricultorService;

    public UserController(UserService userService, AgricultorService agricultorService) {
        this.userService = userService;
        this.agricultorService = agricultorService;
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
}
