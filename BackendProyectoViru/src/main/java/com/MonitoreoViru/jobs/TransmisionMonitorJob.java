package com.MonitoreoViru.jobs;

import com.MonitoreoViru.services.TransmisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransmisionMonitorJob {

    private static final Logger log = LoggerFactory.getLogger(TransmisionMonitorJob.class);

    private final TransmisionService transmisionService;

    public TransmisionMonitorJob(TransmisionService transmisionService) {
        this.transmisionService = transmisionService;
    }

    // fixedDelay: the next run starts 60 s after the previous one finished, so runs never overlap
    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    public void ejecutar() {
        int perdidas = transmisionService.marcarPerdidas();
        if (perdidas > 0) {
            log.warn("{} transmision(es) marcadas como PERDIDA", perdidas);
        }
    }
}
