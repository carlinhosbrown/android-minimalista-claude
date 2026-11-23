package es.minimalista.mayores.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import es.minimalista.mayores.data.Settings
import es.minimalista.mayores.launcher.MainActivity

/**
 * Modo quiosco (lock task). Funciona de dos formas:
 *
 * 1. Pruebas locales: la app es Device Owner (por adb) y se pone ella misma las políticas.
 * 2. Producción: Android Management API es el Device Owner; la política incluye esta app en
 *    `kioskCustomLauncherEnabled`/`lockTaskAllowed` y la lista de apps permitidas.
 *    Entonces [isLockTaskPermitted] es true y aquí solo hay que entrar en el quiosco.
 */
class KioskManager(private val context: Context) {
    private val dpm = context.getSystemService(DevicePolicyManager::class.java)
    private val admin = AdminReceiver.component(context)
    private val settings = Settings(context)

    val isDeviceOwner: Boolean get() = dpm.isDeviceOwnerApp(context.packageName)

    val isLockTaskPermitted: Boolean get() = dpm.isLockTaskPermitted(context.packageName)

    /** Solo si somos Device Owner: lanzador fijo y lista de apps permitidas en el quiosco. */
    fun applyPolicies() {
        if (!isDeviceOwner) return
        try {
            val allowed = (listOf(context.packageName) + settings.visiblePackages + CALL_PACKAGES)
                .distinct().toTypedArray()
            dpm.setLockTaskPackages(admin, allowed)
            dpm.setLockTaskFeatures(
                admin,
                DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
                    DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
                    DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS or
                    DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD,
            )
            val home = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            dpm.addPersistentPreferredActivity(admin, home, ComponentName(context, MainActivity::class.java))
        } catch (e: SecurityException) {
            Log.w(TAG, "No se pudieron aplicar las políticas", e)
        }
    }

    /** Entra en el quiosco si está activado y el sistema lo permite. */
    fun enterIfNeeded(activity: Activity) {
        if (!settings.kioskEnabled || !isLockTaskPermitted || isInLockTask()) return
        try {
            activity.startLockTask()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo entrar en el quiosco", e)
        }
    }

    fun exit(activity: Activity) {
        if (!isInLockTask()) return
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo salir del quiosco", e)
        }
    }

    fun isInLockTask(): Boolean =
        context.getSystemService(ActivityManager::class.java).lockTaskModeState !=
            ActivityManager.LOCK_TASK_MODE_NONE

    /** Solo para pruebas: deja de ser Device Owner para poder desinstalar la app. */
    fun removeDeviceOwner(activity: Activity) {
        if (!isDeviceOwner) return
        exit(activity)
        dpm.clearPackagePersistentPreferredActivities(admin, context.packageName)
        dpm.setLockTaskPackages(admin, emptyArray())
        @Suppress("DEPRECATION")
        dpm.clearDeviceOwnerApp(context.packageName)
    }

    companion object {
        private const val TAG = "KioskManager"

        /** Pantallas de llamada entrante/saliente que deben poder abrirse dentro del quiosco. */
        val CALL_PACKAGES = listOf(
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.incallui", // Xiaomi y AOSP
            "com.samsung.android.incallui",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.contacts",
        )
    }
}
