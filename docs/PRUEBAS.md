# Probar la app en un móvil real

Estos pasos convierten la app en *Device Owner* del móvil por adb, para probar el modo quiosco sin Android Management.

> **Importante:** un móvil solo acepta un Device Owner si **no tiene ninguna cuenta** (ni Google, ni Xiaomi, ni Samsung). Usa un móvil de pruebas o restablécelo de fábrica. Al final explicamos cómo deshacerlo.

## 1. Preparar el ordenador

1. Instala [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) (trae `adb`).
2. Descarga el APK: en GitHub, pestaña **Actions**, último run verde, artefacto `mi-movil-facil-debug` (es un zip con `app-debug.apk`). O compílalo con `./gradlew assembleDebug`.

## 2. Preparar el móvil

1. Restablece de fábrica (o usa uno sin cuentas). En el asistente inicial **sáltate** la cuenta de Google y la de Xiaomi/Samsung. Conecta el wifi.
2. Activa las opciones de desarrollador: *Ajustes → Información del teléfono* y pulsa 7 veces en **Versión de MIUI/HyperOS** (Xiaomi) o **Número de compilación** (Samsung, en *Información de software*).
3. En *Opciones de desarrollador* activa **Depuración USB**.
   - **Xiaomi**: activa también **Instalar vía USB** y **Depuración USB (ajustes de seguridad)**. Xiaomi pide meter una tarjeta SIM y entrar con cuenta Mi para activarlas: entra, actívalas y **cierra la sesión de la cuenta Mi** antes del paso 3.
4. Conecta el móvil por USB y acepta el aviso "¿Permitir depuración USB?".

## 3. Instalar y hacer Device Owner

```bash
adb devices                                   # debe salir tu móvil como "device"
adb install app-debug.apk
adb shell dpm set-device-owner es.minimalista.mayores/.kiosk.AdminReceiver
```

Si sale `Success: Device owner set to package ...`, ya está. Si dice que hay cuentas en el dispositivo, quita todas las cuentas en *Ajustes → Cuentas* y repite.

## 4. Probar

1. Pulsa el botón de inicio y elige **Mi Móvil Fácil** como lanzador (siempre).
2. Deberías ver el reloj y los botones grandes. El móvil queda en modo quiosco: no hay barra de notificaciones ni botón de apps recientes.
3. Ahora puedes añadir la cuenta de Google desde *Familia → Abrir ajustes de Android* (el Device Owner ya está puesto) e instalar WhatsApp, etc.
4. Mantén pulsado **Familia** 3 segundos, crea el PIN y prueba a mostrar u ocultar apps.

Qué comprobar:
- Que las apps ocultas no se pueden abrir.
- Que una llamada entrante se ve y se puede contestar dentro del quiosco.
- Que el botón de inicio siempre vuelve al lanzador.

## 5. Deshacerlo

En **Familia → Quitar Device Owner (solo pruebas)**. Después puedes desinstalar la app con normalidad o con `adb uninstall es.minimalista.mayores`.

## Probar sin Device Owner

Basta con `adb install app-debug.apk` y elegirla como lanzador. Todo funciona menos el quiosco (el modo familiar lo indica).
