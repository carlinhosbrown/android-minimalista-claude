# Mi Móvil Fácil

Lanzador Android para personas mayores: pocas apps, botones grandes y un modo familiar con PIN.
Funciona en móviles normales (prioridad Xiaomi y Samsung baratos, Android 10 o superior) sin desbloquear el bootloader.

## Qué hace la fase 1

- **Modo mayor**: reloj y fecha grandes, y un botón grande por cada app permitida. El botón atrás no saca del lanzador.
- **Apps base**: la primera vez se muestran solas las que estén instaladas: teléfono, WhatsApp, fotos, cámara, Maps, calendario, Instagram y TikTok. Las apps de SMS no se activan nunca solas y en el modo familiar salen marcadas como "no recomendadas".
- **Modo familiar**: se entra manteniendo pulsado 3 segundos el texto "Familia" (abajo a la derecha). La primera vez se crea un PIN de 4 a 8 cifras. Desde ahí se muestran u ocultan apps, se cambia el orden, se cambia el PIN, se activa o quita el modo quiosco y se abren los ajustes de Android. Tras 5 PIN fallidos hay que esperar (30 s, luego el doble cada vez).
- **Modo quiosco (lock task)**: si la app es *Device Owner* (o, en la suscripción, si el gestor remoto lo permite), el móvil queda bloqueado en el lanzador y en las apps permitidas (más las pantallas de llamada).

## Funciona sin servidor

El lanzador con sus dos modos es **gratis y funciona entero solo con el móvil**: no necesita cuenta, internet ni servidor. La gestión remota (app/web familiar) será una suscripción opcional por encima, en un módulo aparte que se engancha por `remote/RemoteManagement.kt`. Sin ese módulo la app usa `NoRemoteManagement` y no cambia nada.

## Propietaria del móvil (Device Owner)

En la versión gratis **no se usa Android Management API**: esta app es la propietaria del móvil (*Device Owner*), instalada por QR al restablecerlo (o por adb en pruebas, ver [docs/PRUEBAS.md](docs/PRUEBAS.md)). Así pone ella misma el quiosco, el lanzador fijo y las actualizaciones automáticas del sistema.

Las apps (WhatsApp, Maps...) se siguen actualizando solas y gratis desde Google Play con la cuenta Google del mayor: la tienda solo se quita del lanzador, no se oculta ni se desactiva.

Si en la suscripción se usa un gestor remoto que sea el Device Owner, la app lo detecta (`isLockTaskPermitted`) y solo entra en quiosco.

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
