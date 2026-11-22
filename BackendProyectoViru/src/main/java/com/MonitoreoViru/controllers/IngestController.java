package com.MonitoreoViru.controllers;

import com.MonitoreoViru.dto.IngestRequestDTO;
import com.MonitoreoViru.services.IngestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Called by the devices / network server. Protected by X-API-Key, not by JWT. */
@RestController
@RequestMapping("/api/ingest")
public class IngestController {

    private final IngestService ingestService;

    public IngestController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("/transacciones")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recibir(@Valid @RequestBody IngestRequestDTO req) {
        ingestService.registrar(req);
    }
}
