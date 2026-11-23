package es.minimalista.mayores.family

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import es.minimalista.mayores.R
import es.minimalista.mayores.data.AppCatalog
import es.minimalista.mayores.data.AppEntry
import es.minimalista.mayores.data.DefaultApps
import es.minimalista.mayores.data.PinHasher
import es.minimalista.mayores.data.Settings
import es.minimalista.mayores.kiosk.KioskManager

/**
 * Modo familiar. Pide el PIN (o lo crea la primera vez) y deja elegir qué apps ve el mayor,
 * en qué orden, y si el quiosco está activo. Se cierra al salir de la pantalla, así que
 * cada entrada vuelve a pedir el PIN.
 */
class FamilyActivity : Activity() {
    private lateinit var settings: Settings
    private lateinit var catalog: AppCatalog
    private lateinit var kiosk: KioskManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(this)
        catalog = AppCatalog(this)
        kiosk = KioskManager(this)
        if (settings.hasPin) askPin() else createPin(onDone = ::showPanel)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    // ---------- PIN ----------

    private fun createPin(onDone: () -> Unit) {
        pinScreen(getString(R.string.pin_crear)) { first ->
            if (!PinHasher.isValidFormat(first)) {
                toast(R.string.pin_corto); return@pinScreen
            }
            pinScreen(getString(R.string.pin_repetir)) { second ->
                if (second == first) {
                    settings.setPin(first); onDone()
                } else {
                    toast(R.string.pin_no_coincide); createPin(onDone)
                }
            }
        }
    }

    private fun askPin() {
        pinScreen(getString(R.string.pin_introducir)) { pin ->
            when (val result = settings.checkPin(pin)) {
                Settings.PinResult.Ok -> showPanel()
                Settings.PinResult.Wrong -> toast(R.string.pin_incorrecto)
                is Settings.PinResult.Locked ->
                    Toast.makeText(this, getString(R.string.pin_bloqueado, result.seconds), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pinScreen(title: String, onSubmit: (String) -> Unit) {
        val root = column().apply { gravity = Gravity.CENTER_HORIZONTAL; setPadding(dp(24), dp(64), dp(24), dp(24)) }
        root.addView(text(title, 26f, bold = true).apply { gravity = Gravity.CENTER })
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(InputFilter.LengthFilter(PinHasher.MAX_LENGTH))
            textSize = 36f
            gravity = Gravity.CENTER
            imeOptions = EditorInfo.IME_ACTION_DONE
            setTextColor(getColor(R.color.texto))
        }
        root.addView(input, LinearLayout.LayoutParams(dp(260), LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(24) })
        val submit = {
            val pin = input.text.toString()
            input.text.clear()
            onSubmit(pin)
        }
        input.setOnEditorActionListener { _, _, _ -> submit(); true }
        root.addView(button(getString(R.string.aceptar)) { submit() })
        setContentView(root)
        input.requestFocus()
    }

    // ---------- Panel ----------

    private fun showPanel() {
        val root = column().apply { setPadding(dp(16), dp(24), dp(16), dp(24)) }
        root.addView(text(getString(R.string.familia_titulo), 30f, bold = true))

        // Estado del quiosco
        val status = when {
            kiosk.isDeviceOwner -> R.string.familia_estado_owner
            kiosk.isLockTaskPermitted -> R.string.familia_estado_permitido
            else -> R.string.familia_estado_sin_owner
        }
        root.addView(text(getString(status), 16f).apply { setPadding(0, dp(12), 0, dp(4)) })
        root.addView(Switch(this).apply {
            text = getString(R.string.familia_kiosco)
            textSize = 18f
            isChecked = settings.kioskEnabled
            isEnabled = kiosk.isLockTaskPermitted
            setOnCheckedChangeListener { _, checked ->
                settings.kioskEnabled = checked
                if (!checked) kiosk.exit(this@FamilyActivity)
            }
        })

        // Apps visibles
        root.addView(text(getString(R.string.familia_apps), 22f, bold = true).apply { setPadding(0, dp(24), 0, dp(8)) })
        val appsBox = column()
        root.addView(appsBox)
        renderApps(appsBox)

        // Acciones
        root.addView(button(getString(R.string.familia_cambiar_pin)) {
            createPin(onDone = ::showPanel)
        })
        root.addView(button(getString(R.string.familia_ajustes)) {
            kiosk.exit(this)
            startActivity(Intent(AndroidSettings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        })
        if (kiosk.isDeviceOwner) {
            root.addView(button(getString(R.string.familia_quitar_owner)) {
                kiosk.removeDeviceOwner(this)
                toast(R.string.familia_owner_quitado)
                showPanel()
            })
        }
        root.addView(button(getString(R.string.familia_salir)) { finish() })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun renderApps(box: LinearLayout) {
        box.removeAllViews()
        val visible = settings.visiblePackages
        val installed = catalog.launchableApps()
        // Primero las visibles en su orden, luego el resto por nombre.
        val ordered = visible.mapNotNull { pkg -> installed.firstOrNull { it.packageName == pkg } } +
            installed.filter { it.packageName !in visible }
        ordered.forEach { box.addView(appRow(it, box)) }
    }

    private fun appRow(app: AppEntry, box: LinearLayout): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))
        }
        row.addView(ImageView(this).apply { setImageDrawable(app.icon) }, LinearLayout.LayoutParams(dp(40), dp(40)))

        val labels = column().apply { setPadding(dp(12), 0, dp(8), 0) }
        labels.addView(text(app.label, 18f))
        if (app.packageName in DefaultApps.notRecommended) {
            labels.addView(text(getString(R.string.familia_no_recomendada), 13f).apply { setTextColor(getColor(R.color.aviso)) })
        }
        row.addView(labels, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val isVisible = app.packageName in settings.visiblePackages
        if (isVisible && settings.visiblePackages.first() != app.packageName) {
            row.addView(Button(this).apply {
                text = getString(R.string.familia_mover_arriba)
                setOnClickListener {
                    val list = settings.visiblePackages.toMutableList()
                    val i = list.indexOf(app.packageName)
                    if (i > 0) {
                        list.removeAt(i); list.add(i - 1, app.packageName)
                        settings.visiblePackages = list
                        renderApps(box)
                    }
                }
            })
        }
        row.addView(Switch(this).apply {
            isChecked = isVisible
            contentDescription = app.label
            setOnCheckedChangeListener { _, checked ->
                val list = settings.visiblePackages.toMutableList()
                if (checked) list.add(app.packageName) else list.remove(app.packageName)
                settings.visiblePackages = list
                kiosk.applyPolicies()
                renderApps(box)
            }
        })
        return row
    }

    // ---------- Utilidades de interfaz ----------

    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

    private fun text(value: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(getColor(R.color.texto))
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        textSize = 18f
        isAllCaps = false
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(12) }
    }

    private fun toast(res: Int) = Toast.makeText(this, res, Toast.LENGTH_LONG).show()

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
