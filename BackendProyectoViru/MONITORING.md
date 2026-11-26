# Monitoring and messaging

Admin supervises 24 devices (3 river flow meters, 20 channel flow meters, 1 weather station) in near
real time; farmers receive irrigation times and flow alerts sent by the admin.

## Endpoints

| Who | Method and path | Notes |
|---|---|---|
| anyone | `POST /api/users/register/admin`, `/register/farmer`, `POST /api/users/login` | unchanged |
| device | `POST /api/ingest/transacciones` | header `X-API-Key`; returns 202 |
| admin | `GET /api/admin/dispositivos/estado` | snapshot of all active devices |
| admin | `GET /api/admin/stream` | Server-Sent Events, event name `lectura` |
| admin | `POST /api/admin/mensajes` | `HORA_RIEGO`, `ALERTA_BAJO_CAUDAL`, `ALERTA_ALTO_CAUDAL`, `ALERTA_METEOROLOGICA` |
| farmer | `GET /api/farmer/mensajes?soloNoLeidos=false&limite=50` | own inbox |
| farmer | `PATCH /api/farmer/mensajes/{id}/leido` | `id` is the entrega id from the inbox |

```bash
# a device reports (assumed payload, see IngestRequestDTO)
curl -X POST http://localhost:8080/api/ingest/transacciones \
  -H "X-API-Key: $VIRU_INGEST_API_KEY" -H "Content-Type: application/json" \
  -d '{"devEui":"A81758FFFE05B2C1","variables":[{"nombre":"caudal","valor":1.254,"unidad":"m3/s"}]}'

# admin sends an irrigation time to one junta
curl -X POST http://localhost:8080/api/admin/mensajes \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"tipo":"HORA_RIEGO","alcance":"JUNTA","juntaReganteId":3,"riegoEn":"2026-11-21T11:00:00Z"}'

# admin sends a low-flow alert to a whole region
curl -X POST http://localhost:8080/api/admin/mensajes \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"tipo":"ALERTA_BAJO_CAUDAL","alcance":"REGION","regionId":1,"nota":"Canal 7 por debajo del minimo"}'
```

## Assumptions to verify

1. `Region`: table `region` (`id`, `nombre`) and column `junta_regante.region_id`.
2. `dispositivo.tipo` holds `CAUDALIMETRO_RIO`, `CAUDALIMETRO_CANAL` or `ESTACION_METEO`.
3. `transaccion.recibido_nube_en` and `guardado_bd_en` are nullable (the SQL script makes them so).
4. The device payload (`IngestRequestDTO`) is a guess; adjust it to what the devices really send.
5. The browser `EventSource` cannot send an `Authorization` header: use a fetch-based SSE client.

## How the transaction slots work

- Each `transaccion` row is one expected check-in: `esperado_en`, `limite_en`
  (= `esperado_en` + the device's tolerance), `estado`.
- A message before `limite_en` closes the oldest pending slot as `RECIBIDA`, after it as `TARDIA`;
  the next slot is created one `intervalo_segundos` later.
- `TransmisionMonitorJob` runs every minute: pending slots past `limite_en` become `PERDIDA`, and every
  active device always has a pending slot. A device whose last finished slot is `PERDIDA` shows as
  `SIN_SENAL` on the dashboard.
- The same message delivered twice is ignored (same device + `medidoEn`); a unique index backs this up.

## Known limits

- The live stream keeps connections in memory: one server instance only.
- After a long downtime, missed slots are not back-filled; the schedule restarts.
- No rate limiting or push / SMS delivery to farmers.

## Predios y alertas de caudal en TB2

1. En PostgreSQL, toma un respaldo de la base de PC4 y aplica `sql/002_predios_y_alertas.sql` con el rol dueño. La migración usa el esquema existente `app_pc4`. Conserva los avisos, cuentas y lecturas previos.
2. Instala el nuevo JAR de Spring Boot. En la web, abre **Predios y canales** con una cuenta administradora. Registra el código y nombre de cada predio, su agricultor y sus canales según el padrón. El catálogo toma los nombres del plano PCG-01.
3. Para la demo, aplica también `sql/demo_series_tb2.sql` con el rol dueño en la base de laboratorio. Carga doce muestras por dispositivo y deja el historial disponible al volver a abrir el panel. La importación histórica no genera alertas.
4. Envía una lectura por la API de ingesta con `simulada: true` y una variable `caudal` en `L/s`. Para el laboratorio, los canales entran en alerta en **120 L/s** y vuelven al estado normal en **105 L/s o menos**. Una lectura de 135 L/s genera un aviso para los agricultores asignados al canal. Una lectura siguiente de 150 L/s mantiene el aviso existente. Una lectura de 100 L/s cierra el episodio, y una nueva lectura de 135 L/s abre otro.

La API también acepta caudal en `m3/s` y `m³/s`. `distance_mm` representa distancia al agua. Las reglas de campo se configuran por canal con `simulada = false` y valores validados para ese sitio.

| Ruta | Acceso y función |
|---|---|
| `GET /api/catalogo/riego` | Catálogo de juntas y regiones para registro y avisos. |
| `GET /api/admin/predios` | Agricultores, predios y canales disponibles para asignación. |
| `PUT /api/admin/predios/{codigo}` | Crea o actualiza un predio con `agricultorId`, `nombre` y `canalIds`. |
| `GET /api/admin/dispositivos/{id}/historial` | Historial ordenado del dispositivo. |
| `GET /api/farmer/predios` | Devuelve únicamente los predios y canales del agricultor autenticado. |

### Correo

La alerta y sus entregas se guardan en la transacción de ingesta. Un proceso de Spring lee la cola persistente `correo_alerta` después de confirmar esa transacción. Cada envío se registra por entrega y admite hasta cuatro intentos. Los avisos automáticos de laboratorio usan un buzón de prueba. Los avisos de campo se dirigen al correo de la cuenta asignada al predio.

Configura estas variables en el archivo de entorno del servicio de Azure.

```dotenv
VIRU_CORREO_HABILITADO=true
VIRU_CORREO_REMITENTE=correo-autorizado-del-proveedor
VIRU_CORREO_DESTINO_PRUEBA=tu-buzon-de-prueba
VIRU_SMTP_HOST=servidor-smtp
VIRU_SMTP_PORT=587
VIRU_SMTP_USUARIO=usuario-smtp
VIRU_SMTP_CLAVE=clave-smtp
```

Conserva la autenticación y STARTTLS para el proveedor de Azure. La aplicación arranca con correo desactivado por defecto. Activa el envío al iniciar el servicio con las variables anteriores. En modo de laboratorio, un mensaje simulado sin buzón de prueba se registra como omitido.

El envío usa `JavaMailSender` y `SimpleMailMessage`, siguiendo el servicio de correo de Yachani. [Código de referencia](https://github.com/KeniChris/Yachani-api/blob/main/src/main/java/com/yachaniapi/usuario/service/CorreoService.java)
