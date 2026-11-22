package com.MonitoreoViru.controllers;

import com.MonitoreoViru.dto.EnviarMensajeRequestDTO;
import com.MonitoreoViru.dto.MensajeEnviadoDTO;
import com.MonitoreoViru.services.MensajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin only (SecurityConfig). */
@RestController
@RequestMapping("/api/admin/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @PostMapping
    public ResponseEntity<MensajeEnviadoDTO> enviar(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody EnviarMensajeRequestDTO req) {
        Long autorId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(mensajeService.enviar(autorId, req));
    }
}
