# Mi Móvil Fácil

Lanzador Android para personas mayores: pocas apps, botones grandes y un modo familiar con PIN.
Funciona en móviles normales (prioridad Xiaomi y Samsung baratos, Android 10 o superior) sin desbloquear el bootloader.

## Qué hace la fase 1

- **Modo mayor**: reloj y fecha grandes, y un botón grande por cada app permitida. El botón atrás no saca del lanzador.
- **Apps base**: la primera vez se muestran solas las que estén instaladas: teléfono, WhatsApp, fotos, cámara, Maps, calendario, Instagram y TikTok. Las apps de SMS no se activan nunca solas y en el modo familiar salen marcadas como "no recomendadas".
- **Modo familiar**: se entra manteniendo pulsado 3 segundos el texto "Familia" (abajo a la derecha). La primera vez se crea un PIN de 4 a 8 cifras. Desde ahí se muestran u ocultan apps, se cambia el orden, se cambia el PIN, se activa o quita el modo quiosco y se abren los ajustes de Android. Tras 5 PIN fallidos hay que esperar (30 s, luego el doble cada vez).
- **Modo quiosco (lock task)**: si la app es *Device Owner* o la política de Android Management la permite, el móvil queda bloqueado en el lanzador y en las apps permitidas (más las pantallas de llamada).

## Funciona sin servidor

El lanzador con sus dos modos es **gratis y funciona entero solo con el móvil**: no necesita cuenta, internet ni servidor. La gestión remota (app/web familiar) será una suscripción opcional por encima, en un módulo aparte que se engancha por `remote/RemoteManagement.kt`. Sin ese módulo la app usa `NoRemoteManagement` y no cambia nada.

## Cómo se gestionará en producción

En producción el móvil se registra con **Android Management API** (Google Play gestionado). Google es el *Device Owner* y aplica la política; esta app es el lanzador de quiosco. En la política hay que permitir esta app en lock task (`kioskCustomLauncherEnabled` o `installType: KIOSK` / `lockTaskAllowed: true`) y añadir las apps permitidas. La app lo detecta sola y entra en quiosco.

Para probar sin Android Management, la app puede ser Device Owner por adb: ver [docs/PRUEBAS.md](docs/PRUEBAS.md).

## Compilar

Necesitas Android Studio (o JDK 17 y el SDK de Android 35).

```bash
./gradlew assembleDebug          # APK en app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # pruebas
```

Cada push a GitHub compila el APK en *Actions* y lo deja para descargar como `mi-movil-facil-debug`.

## Próximas fases

- Filtro de llamadas (`CallScreeningService`): solo contactos; los desconocidos se desvían al familiar elegido.
- Recordatorios de medicinas y citas desde Google Calendar del mayor.
- Panel familiar remoto (web/app): apps, agenda, actualizaciones, localización.
- Navegador opcional con filtro DNS.
