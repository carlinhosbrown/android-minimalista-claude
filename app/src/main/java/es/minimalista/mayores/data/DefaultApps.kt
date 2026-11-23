package es.minimalista.mayores.data

/**
 * Apps base del modo mayor y paquetes conocidos en Xiaomi, Samsung y Android de Google.
 * Para cada función se elige el primer paquete instalado de la lista.
 */
object DefaultApps {

    enum class Role { TELEFONO, WHATSAPP, FOTOS, CAMARA, MAPAS, CALENDARIO, INSTAGRAM, TIKTOK }

    val candidates: Map<Role, List<String>> = linkedMapOf(
        Role.TELEFONO to listOf(
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.contacts", // Xiaomi (MIUI/HyperOS) usa este paquete para el teléfono
            "com.android.dialer",
        ),
        Role.WHATSAPP to listOf("com.whatsapp"),
        Role.FOTOS to listOf(
            "com.google.android.apps.photos",
            "com.miui.gallery",
            "com.sec.android.gallery3d",
        ),
        Role.CAMARA to listOf(
            "com.android.camera", // Xiaomi
            "com.sec.android.app.camera",
            "com.google.android.GoogleCamera",
            "com.android.camera2",
        ),
        Role.MAPAS to listOf("com.google.android.apps.maps"),
        Role.CALENDARIO to listOf(
            "com.google.android.calendar",
            "com.samsung.android.calendar",
            "com.android.calendar",
        ),
        Role.INSTAGRAM to listOf("com.instagram.android"),
        Role.TIKTOK to listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"),
    )

    /** Apps de SMS: no se recomiendan por el riesgo de estafas (phishing). */
    val notRecommended: Set<String> = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms", // Xiaomi
        "com.android.messaging",
    )

    /**
     * Elige las apps visibles por defecto, en el orden de [candidates].
     * [preferredDialer] y [preferredCamera] son las que el sistema tiene como predeterminadas.
     */
    fun pickDefaults(
        installed: Set<String>,
        preferredDialer: String? = null,
        preferredCamera: String? = null,
    ): List<String> = candidates.mapNotNull { (role, packages) ->
        val preferred = when (role) {
            Role.TELEFONO -> preferredDialer
            Role.CAMARA -> preferredCamera
            else -> null
        }
        preferred?.takeIf { it in installed } ?: packages.firstOrNull { it in installed }
    }.distinct()
}
