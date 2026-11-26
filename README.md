# proyecto-viru-services
Backend de pagina web para monitoreo de la cuenca viru comprendida por: Huacapongo, Choloque y Queneto. Proyecto universitario.

## Mapa y alertas de caudal

En una base TB2 con `002_predios_y_alertas.sql` aplicada, ejecuta `BackendProyectoViru/sql/003_mapa_y_alertas.sql` como propietario del esquema `app_pc4` antes de iniciar el nuevo backend. Para el laboratorio, `sql/demo_series_tb2.sql` carga el historial y los umbrales simulados.

`GET /api/farmer/predios` conserva los predios y canales propios. Agrega la comisión del agricultor y el caudal de los canales asociados a predios de esa comisión. El mapa compartido muestra el canal, la última lectura y su vigencia.

La ingesta acepta caudal en L/s o m³/s. Los umbrales de entrada y retorno se configuran por canal y origen en `regla_caudal`. Para las lecturas de campo (`simulada = false`), los valores deben corresponder a la calibración del canal:

```text
0 ≤ umbral_bajo_lps < retorno_bajo_lps ≤ retorno_lps < umbral_alto_lps
```

El aviso se genera al entrar en caudal bajo o alto. Las lecturas vencidas y las reentregas se excluyen del control de avisos. El siguiente aviso de un mismo tipo requiere cruzar su umbral después de salir del estado anterior. Las entregas de la bandeja y la cola de correo se guardan con la medición. El envío efectivo usa el SMTP configurado con `VIRU_CORREO_HABILITADO`. Los avisos simulados solo pueden salir al buzón de prueba configurado.
