package es.minimalista.mayores.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import es.minimalista.mayores.R
import es.minimalista.mayores.data.AppCatalog
import es.minimalista.mayores.data.AppEntry
import es.minimalista.mayores.data.Settings
import es.minimalista.mayores.family.FamilyActivity
import es.minimalista.mayores.kiosk.KioskManager

/** Pantalla de inicio del modo mayor: reloj grande y botones grandes, uno por app. */
class MainActivity : Activity() {
    private lateinit var settings: Settings
    private lateinit var catalog: AppCatalog
    private lateinit var kiosk: KioskManager
    private lateinit var list: LinearLayout
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settings = Settings(this)
        catalog = AppCatalog(this)
        kiosk = KioskManager(this)
        list = findViewById(R.id.lista_apps)
        setupFamilyButton(findViewById(R.id.boton_familia))
    }

    override fun onResume() {
        super.onResume()
        catalog.ensureDefaults(settings)
        kiosk.applyPolicies()
        kiosk.enterIfNeeded(this)
        render()
    }

    // El mayor no puede "salir" del lanzador con el botón atrás.
    @Deprecated("Se mantiene para Android 10-12")
    override fun onBackPressed() = Unit

    private fun render() {
        list.removeAllViews()
        val apps = settings.visiblePackages.mapNotNull { catalog.entryFor(it) }
        if (apps.isEmpty()) {
            list.addView(TextView(this).apply {
                text = getString(R.string.sin_apps)
                textSize = 26f
                gravity = Gravity.CENTER
                setPadding(0, dp(48), 0, 0)
            })
            return
        }
        apps.forEach { list.addView(tile(it)) }
    }

    private fun tile(app: AppEntry): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.tarjeta_fondo)
        setPadding(dp(20), dp(16), dp(20), dp(16))
        minimumHeight = dp(104)
        isClickable = true
        isFocusable = true
        contentDescription = app.label
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(14) }

        addView(ImageView(context).apply {
            setImageDrawable(app.icon)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, LinearLayout.LayoutParams(dp(72), dp(72)))

        addView(TextView(context).apply {
            text = app.label
            textSize = 30f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(24), 0, 0, 0)
            maxLines = 2
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (!catalog.launch(app.packageName)) {
                Toast.makeText(context, R.string.app_no_disponible, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Hay que mantener pulsado 3 segundos, para que el mayor no entre sin querer. */
    private fun setupFamilyButton(button: View) {
        val open = Runnable {
            button.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            startActivity(Intent(this, FamilyActivity::class.java))
        }
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> handler.postDelayed(open, HOLD_MILLIS)
                MotionEvent.ACTION_UP -> { handler.removeCallbacks(open); v.performClick() }
                MotionEvent.ACTION_CANCEL -> handler.removeCallbacks(open)
            }
            true
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val HOLD_MILLIS = 3000L
    }
}
