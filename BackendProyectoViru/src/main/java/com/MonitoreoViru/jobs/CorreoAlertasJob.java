package com.MonitoreoViru.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@ConditionalOnProperty(prefix = "viru.correo", name = "habilitado", havingValue = "true")
public class CorreoAlertasJob {

    private static final Logger log = LoggerFactory.getLogger(CorreoAlertasJob.class);
    private static final int MAX_INTENTOS = 4;

    private record Pendiente(Long id, int intentos, String correo, String nota, String canal,
                             OffsetDateTime medidoEn, boolean simulada) {}

    private final JdbcClient jdbc;
    private final JavaMailSender mail;
    private final String remitente;
    private final String destinoPrueba;

    public CorreoAlertasJob(JdbcClient jdbc, JavaMailSender mail,
                            @Value("${viru.correo.remitente}") String remitente,
                            @Value("${viru.correo.destino-prueba}") String destinoPrueba,
                            @Value("${spring.mail.host}") String smtp) {
        if (remitente.isBlank() || smtp.isBlank()) {
            throw new IllegalArgumentException("Configura el remitente y el servidor SMTP de Virú.");
        }
        this.jdbc = jdbc;
        this.mail = mail;
        this.remitente = remitente;
        this.destinoPrueba = destinoPrueba;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void procesar() {
        var pendientes = jdbc.sql("""
                SELECT q.entrega_id AS id, q.intentos, u.correo, m.nota, d.codigo AS canal,
                       m.medido_en, m.simulada
                FROM correo_alerta q
                JOIN entrega e ON e.id = q.entrega_id
                JOIN mensaje m ON m.id = e.mensaje_id
                JOIN dispositivo d ON d.id = m.canal_id
                JOIN agricultor a ON a.id = e.agricultor_id
                JOIN usuario u ON u.id = a.user_id
                WHERE q.estado = 'PENDIENTE' AND q.proximo_intento <= now()
                ORDER BY q.proximo_intento, q.entrega_id
                LIMIT 10 FOR UPDATE OF q SKIP LOCKED
                """).query(Pendiente.class).list();
        pendientes.forEach(this::enviar);
    }

    private void enviar(Pendiente pendiente) {
        if (pendiente.simulada() && destinoPrueba.isBlank()) {
            actualizar(pendiente, "OMITIDO", pendiente.intentos());
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(pendiente.simulada() ? destinoPrueba : pendiente.correo());
        mensaje.setSubject((pendiente.simulada() ? "[Simulación] " : "")
                + "Riego Virú · Caudal alto en " + pendiente.canal());
        mensaje.setText((pendiente.simulada() ? "Prueba de laboratorio de Riego Virú.\n\n" : "Riego Virú\n\n")
                + pendiente.nota() + "\nMedición: " + pendiente.medidoEn().toInstant() + " (UTC).\n"
                + "Consulta el aviso en la bandeja de Riego Virú.");

        int intentos = pendiente.intentos() + 1;
        try {
            mail.send(mensaje);
            actualizar(pendiente, "ENVIADO", intentos);
        } catch (MailException e) {
            actualizar(pendiente, intentos < MAX_INTENTOS ? "PENDIENTE" : "FALLIDO", intentos);
            log.warn("No se pudo enviar el correo de la entrega {} (intento {}).", pendiente.id(), intentos);
        }
    }

    private void actualizar(Pendiente pendiente, String estado, int intentos) {
        jdbc.sql("""
                UPDATE correo_alerta
                SET estado = :estado, intentos = :intentos,
                    proximo_intento = now() + make_interval(secs => :demora),
                    enviado_en = CASE WHEN :estado = 'ENVIADO' THEN now() ELSE NULL END
                WHERE entrega_id = :id
                """).param("estado", estado).param("intentos", intentos).param("demora", intentos * 30)
                .param("id", pendiente.id()).update();
    }
}
