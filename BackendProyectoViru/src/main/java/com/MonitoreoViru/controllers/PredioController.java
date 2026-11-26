package com.MonitoreoViru.controllers;

import com.MonitoreoViru.services.PredioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PredioController {
    private final PredioService predios;

    public PredioController(PredioService predios) {
        this.predios = predios;
    }

    @GetMapping("/catalogo/riego")
    public PredioService.Catalogo catalogo() {
        return predios.catalogo();
    }

    @GetMapping("/admin/predios")
    public PredioService.Panel panel() {
        return predios.panel();
    }

    @PutMapping("/admin/predios/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guardar(@PathVariable String codigo, @Valid @RequestBody PredioService.Asignacion asignacion) {
        predios.guardar(codigo, asignacion);
    }

    @GetMapping("/farmer/predios")
    public PredioService.MisPredios misPredios(@AuthenticationPrincipal Jwt jwt) {
        return predios.misPredios(Long.valueOf(jwt.getSubject()));
    }
}
