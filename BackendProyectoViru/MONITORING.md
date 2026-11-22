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
| admin | `POST /api/admin/mensajes` | `HORA_RIEGO`, `ALERTA_BAJO_CAUDAL`, `ALERTA_ALTO_CAUDAL` |
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
- No automated tests, no rate limiting, no push / SMS delivery to farmers.
