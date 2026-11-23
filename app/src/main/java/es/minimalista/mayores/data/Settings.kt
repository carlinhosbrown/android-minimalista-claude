package es.minimalista.mayores.data

import android.content.Context

/** Configuración local del lanzador. Más adelante se sincronizará con el panel familiar remoto. */
class Settings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("mayores", Context.MODE_PRIVATE)

    var initialized: Boolean
        get() = prefs.getBoolean(KEY_INITIALIZED, false)
        set(value) = prefs.edit().putBoolean(KEY_INITIALIZED, value).apply()

    /** Paquetes visibles en el modo mayor, en el orden en que se muestran. */
    var visiblePackages: List<String>
        get() = prefs.getString(KEY_VISIBLE, "").orEmpty().split(',').filter { it.isNotBlank() }
        set(value) = prefs.edit().putString(KEY_VISIBLE, value.distinct().joinToString(",")).apply()

    var kioskEnabled: Boolean
        get() = prefs.getBoolean(KEY_KIOSK, true)
        set(value) = prefs.edit().putBoolean(KEY_KIOSK, value).apply()

    val hasPin: Boolean get() = prefs.contains(KEY_PIN_HASH)

    fun setPin(pin: String) {
        val salt = PinHasher.newSalt()
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, PinHasher.hash(pin, salt))
            .putInt(KEY_FAILS, 0)
            .putLong(KEY_LOCKED_UNTIL, 0)
            .apply()
    }

    /** Comprueba el PIN y lleva la cuenta de fallos. */
    fun checkPin(pin: String, nowMillis: Long = System.currentTimeMillis()): PinResult {
        val lockedUntil = prefs.getLong(KEY_LOCKED_UNTIL, 0)
        if (nowMillis < lockedUntil) return PinResult.Locked((lockedUntil - nowMillis + 999) / 1000)

        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return PinResult.Wrong
        val hash = prefs.getString(KEY_PIN_HASH, null) ?: return PinResult.Wrong
        if (PinHasher.matches(pin, salt, hash)) {
            prefs.edit().putInt(KEY_FAILS, 0).putLong(KEY_LOCKED_UNTIL, 0).apply()
            return PinResult.Ok
        }
        val fails = prefs.getInt(KEY_FAILS, 0) + 1
        val wait = PinLockout.waitSecondsAfter(fails)
        prefs.edit().putInt(KEY_FAILS, fails).putLong(KEY_LOCKED_UNTIL, nowMillis + wait * 1000).apply()
        return if (wait > 0) PinResult.Locked(wait) else PinResult.Wrong
    }

    sealed interface PinResult {
        data object Ok : PinResult
        data object Wrong : PinResult
        data class Locked(val seconds: Long) : PinResult
    }

    private companion object {
        const val KEY_INITIALIZED = "initialized"
        const val KEY_VISIBLE = "visible_packages"
        const val KEY_KIOSK = "kiosk_enabled"
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
        const val KEY_FAILS = "pin_fails"
        const val KEY_LOCKED_UNTIL = "pin_locked_until"
    }
}
