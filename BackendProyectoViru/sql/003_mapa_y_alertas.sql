\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path = app_pc4, public;

ALTER TABLE regla_caudal
    ADD COLUMN IF NOT EXISTS umbral_bajo_lps numeric(14,4),
    ADD COLUMN IF NOT EXISTS retorno_bajo_lps numeric(14,4),
    ADD COLUMN IF NOT EXISTS en_alerta_baja boolean NOT NULL DEFAULT false;

ALTER TABLE regla_caudal DROP CONSTRAINT IF EXISTS ck_regla_caudal_bajo;
ALTER TABLE regla_caudal ADD CONSTRAINT ck_regla_caudal_bajo CHECK (
    NOT (en_alerta AND en_alerta_baja) AND
    ((umbral_bajo_lps IS NULL AND retorno_bajo_lps IS NULL AND NOT en_alerta_baja) OR
     (umbral_bajo_lps IS NOT NULL AND retorno_bajo_lps IS NOT NULL
      AND umbral_bajo_lps >= 0 AND umbral_bajo_lps < retorno_bajo_lps
      AND retorno_bajo_lps <= retorno_lps)));

ALTER TABLE mensaje DROP CONSTRAINT ck_mensaje_origen;
ALTER TABLE mensaje ADD CONSTRAINT ck_mensaje_origen CHECK (
    (alcance <> 'CANAL' AND autor_id IS NOT NULL AND medido_en IS NULL
     AND caudal_lps IS NULL AND umbral_lps IS NULL AND NOT simulada) OR
    (alcance = 'CANAL' AND autor_id IS NULL AND medido_en IS NOT NULL
     AND caudal_lps IS NOT NULL AND umbral_lps IS NOT NULL
     AND ((tipo = 'ALERTA_ALTO_CAUDAL' AND umbral_lps > 0 AND caudal_lps >= umbral_lps) OR
          (tipo = 'ALERTA_BAJO_CAUDAL' AND caudal_lps >= 0 AND caudal_lps <= umbral_lps))));

DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'viru_pc4_app') THEN
        GRANT UPDATE (en_alerta_baja) ON regla_caudal TO viru_pc4_app;
    END IF;
END $$;

COMMIT;
