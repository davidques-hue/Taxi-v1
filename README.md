# Taxi Caja v1
Primera versión Android nativa para registrar ingresos y egresos de taxi.

## Incluye
- Teclado numérico siempre visible y registro rápido de ingresos.
- 3 montos rápidos configurables.
- Egresos con descripción y categoría.
- Totales y ganancia del día.
- Historial de movimientos.
- Informes de día, semana y mes.
- Conductores.
- Móviles con número y patente.
- Inicio/cierre de turno con kilometraje.
- Mantenimiento por móvil/patente, próximo km/fecha y opción de contabilizar como egreso.
- Base SQLite local: funciona sin internet.

## Abrir y generar APK
Abrir la carpeta raíz con Android Studio. Esperar sincronización de Gradle y usar Build > Build APK(s).
Requiere Android SDK 36. El proyecto usa minSdk 24 y targetSdk 36.

## Nota
Esta entrega es la v1 funcional. PDF/compartir, respaldo/restauración, PIN, metas, modo oscuro y estadísticas avanzadas quedan preparados para una siguiente iteración.

## Generar APK automáticamente con GitHub
El proyecto incluye `.github/workflows/build-apk.yml`. Al subirlo a un repositorio GitHub en la rama `main`, Actions compila `app-debug.apk` y lo deja como artefacto `TaxiCaja-v1-APK`.
