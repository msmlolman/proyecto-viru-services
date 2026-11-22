package com.MonitoreoViru.controllers;

import com.MonitoreoViru.dto.NotificacionDTO;
import com.MonitoreoViru.services.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Farmer only (SecurityConfig: /api/farmer/** requires ROLE_FARMER). */
@RestController
@RequestMapping("/api/farmer/mensajes")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public List<NotificacionDTO> bandeja(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam(defaultValue = "false") boolean soloNoLeidos,
                                         @RequestParam(defaultValue = "50") int limite) {
        return notificacionService.bandeja(Long.valueOf(jwt.getSubject()), soloNoLeidos, limite);
    }

    @PatchMapping("/{id}/leido")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLeido(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        notificacionService.marcarLeida(Long.valueOf(jwt.getSubject()), id);
    }
}
