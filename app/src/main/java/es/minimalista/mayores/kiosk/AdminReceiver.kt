package es.minimalista.mayores.kiosk

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Receptor de administrador. Con `adb shell dpm set-device-owner` la app pasa a ser Device Owner. */
class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        KioskManager(context).applyPolicies()
    }

    companion object {
        fun component(context: Context) = ComponentName(context, AdminReceiver::class.java)
    }
}
