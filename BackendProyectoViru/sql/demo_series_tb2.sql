\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path = app_pc4, public;

-- Valores sintéticos. La importación llena el historial y no ejecuta la ingesta ni envía avisos.
CREATE TEMP TABLE muestras_demo ON COMMIT DROP AS
SELECT d.id AS dispositivo_id, d.codigo, d.tipo, s.n,
       date_trunc('minute', now()) - make_interval(secs => s.n * d.intervalo_segundos) AS medido_en,
       d.tolerancia_segundos
FROM dispositivo d
CROSS JOIN generate_series(0, 11) s(n)
WHERE d.activo AND d.codigo ~ '^(CAN-(0[1-9]|1[0-9]|20)|HID-0[1-3]|MET-01)$';

WITH recibidas AS (
    INSERT INTO transaccion (dispositivo_id, esperado_en, limite_en, estado, recibido_nube_en, guardado_bd_en)
    SELECT dispositivo_id, medido_en, medido_en + make_interval(secs => tolerancia_segundos),
           'RECIBIDA', medido_en, now()
    FROM muestras_demo
    ON CONFLICT DO NOTHING
    RETURNING id, dispositivo_id, esperado_en
)
INSERT INTO lectura (transaccion_id, dispositivo_id, variable, valor, unidad, medido_en, simulada)
SELECT t.id, m.dispositivo_id, v.nombre, v.valor, v.unidad, m.medido_en, true
FROM recibidas t
JOIN muestras_demo m ON m.dispositivo_id = t.dispositivo_id AND m.medido_en = t.esperado_en
CROSS JOIN LATERAL (
    SELECT 'battery_pct', 88 + m.n % 8, '%'
    UNION ALL
    SELECT 'caudal', 24 + (m.dispositivo_id % 20) * 3 + (m.n * 7) % 19, 'L/s'
        WHERE m.tipo = 'CAUDALIMETRO_CANAL'
    UNION ALL
    SELECT 'caudal', 275 + (m.dispositivo_id % 10) * 22 + m.n * 7, 'L/s'
        WHERE m.tipo = 'CAUDALIMETRO_RIO'
    UNION ALL
    SELECT 'distance_mm', 1260 - m.n * 6 + (m.dispositivo_id % 5) * 22, 'mm'
        WHERE m.tipo = 'CAUDALIMETRO_RIO'
    UNION ALL
    SELECT 'temperatura', 22.5 + (m.n % 5) * 0.6, '°C' WHERE m.tipo = 'ESTACION_METEO'
    UNION ALL
    SELECT 'humedad', 63 + m.n % 9, '%' WHERE m.tipo = 'ESTACION_METEO'
    UNION ALL
    SELECT 'precipitacion', (m.n % 4) * 0.4, 'mm' WHERE m.tipo = 'ESTACION_METEO'
    UNION ALL
    SELECT 'viento', 2.2 + (m.n % 6) * 0.5, 'm/s' WHERE m.tipo = 'ESTACION_METEO'
    UNION ALL
    SELECT 'presion', 1008 + (m.n % 5) * 0.3, 'hPa' WHERE m.tipo = 'ESTACION_METEO'
) AS v(nombre, valor, unidad)
ON CONFLICT DO NOTHING;

INSERT INTO regla_caudal (canal_id, simulada, umbral_alto_lps, retorno_lps,
                         umbral_bajo_lps, retorno_bajo_lps)
SELECT dispositivo_id, true, 120, 105, 35, 45 FROM canal
ON CONFLICT (canal_id, simulada) DO UPDATE
SET umbral_bajo_lps = excluded.umbral_bajo_lps, retorno_bajo_lps = excluded.retorno_bajo_lps
WHERE regla_caudal.umbral_bajo_lps IS NULL AND regla_caudal.retorno_lps >= excluded.retorno_bajo_lps;

SELECT count(DISTINCT dispositivo_id) AS sensores,
       count(DISTINCT (dispositivo_id, medido_en)) AS muestras,
       count(*) AS valores
FROM lectura
WHERE simulada;

COMMIT;
