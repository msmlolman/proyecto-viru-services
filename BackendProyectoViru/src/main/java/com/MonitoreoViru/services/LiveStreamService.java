package com.MonitoreoViru.services;

import com.MonitoreoViru.dto.LecturaEnVivoDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the open admin connections and pushes every stored transaction to them.
 * In-memory: fine for ONE server instance. With several instances, broadcast through
 * Redis pub/sub or Postgres LISTEN/NOTIFY instead.
 */
@Service
public class LiveStreamService {

    private static final long TIMEOUT_MS = 30 * 60 * 1000L; // the browser reconnects by itself

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    /** Runs only after IngestService's transaction has committed (default phase AFTER_COMMIT). */
    @TransactionalEventListener
    public void onLectura(LecturaEnVivoDTO event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("lectura").data(event));
            } catch (IOException | RuntimeException e) {
                emitters.remove(emitter);
            }
        }
    }

    /** Keeps proxies from closing idle connections. */
    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("keepalive"));
            } catch (IOException | RuntimeException e) {
                emitters.remove(emitter);
            }
        }
    }
}
