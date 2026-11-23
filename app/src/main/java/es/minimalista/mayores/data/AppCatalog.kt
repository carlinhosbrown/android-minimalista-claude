package es.minimalista.mayores.data

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.telecom.TelecomManager

data class AppEntry(val packageName: String, val label: String, val icon: Drawable)

/** Lee las apps instaladas que se pueden abrir desde un lanzador. */
class AppCatalog(private val context: Context) {
    private val pm = context.packageManager

    fun launchableApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { AppEntry(it.packageName, it.loadLabel(pm).toString(), it.loadIcon(pm)) }
            .sortedBy { it.label.lowercase() }
    }

    fun entryFor(packageName: String): AppEntry? = try {
        val info = pm.getApplicationInfo(packageName, 0)
        if (pm.getLaunchIntentForPackage(packageName) == null) null
        else AppEntry(packageName, pm.getApplicationLabel(info).toString(), pm.getApplicationIcon(info))
    } catch (e: Exception) {
        null
    }

    fun launch(packageName: String): Boolean {
        val intent = pm.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            context.startActivity(intent); true
        } catch (e: Exception) {
            false
        }
    }

    fun defaultDialer(): String? =
        context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage

    fun defaultCamera(): String? =
        pm.resolveActivity(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), 0)
            ?.activityInfo?.packageName?.takeIf { it != "android" }

    /** La primera vez, deja visibles las apps base que estén instaladas. */
    fun ensureDefaults(settings: Settings) {
        if (settings.initialized) return
        val installed = launchableApps().map { it.packageName }.toSet()
        settings.visiblePackages = DefaultApps.pickDefaults(installed, defaultDialer(), defaultCamera())
        settings.initialized = true
    }
}
