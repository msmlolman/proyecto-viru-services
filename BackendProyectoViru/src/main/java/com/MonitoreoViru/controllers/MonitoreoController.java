package com.MonitoreoViru.controllers;

import com.MonitoreoViru.dto.DispositivoEstadoDTO;
import com.MonitoreoViru.services.LiveStreamService;
import com.MonitoreoViru.services.MonitoreoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/** Admin only (enforced in SecurityConfig: /api/admin/** requires ROLE_ADMIN). */
@RestController
@RequestMapping("/api/admin")
public class MonitoreoController {

    private final MonitoreoService monitoreoService;
    private final LiveStreamService liveStreamService;

    public MonitoreoController(MonitoreoService monitoreoService, LiveStreamService liveStreamService) {
        this.monitoreoService = monitoreoService;
        this.liveStreamService = liveStreamService;
    }

    /** Load this first, then open the stream. Also usable alone by polling every 60 s. */
    @GetMapping("/dispositivos/estado")
    public List<DispositivoEstadoDTO> estado() {
        return monitoreoService.estadoActual();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return liveStreamService.subscribe();
    }
}
