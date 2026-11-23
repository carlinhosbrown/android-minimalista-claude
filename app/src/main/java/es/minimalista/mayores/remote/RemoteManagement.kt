package es.minimalista.mayores.remote

/**
 * Punto de enganche de la gestión remota (app/web familiar, de pago por suscripción).
 *
 * La app es completa sin esto: el modo mayor y el modo familiar funcionan solo con el móvil,
 * sin cuenta ni servidor (nivel gratis). La gestión remota irá en un módulo Gradle aparte
 * (`:remote`) que implemente esta interfaz; si no está, se usa [NoRemoteManagement].
 *
 * Regla: el código local nunca espera al servidor. Lo remoto solo propone cambios
 * (apps visibles, contactos, citas...) que se guardan en la configuración local.
 */
interface RemoteManagement {
    /** true si el móvil está vinculado a una suscripción de gestión. */
    val isLinked: Boolean

    /** Avisa de que la configuración local ha cambiado (para subirla al panel). */
    fun onLocalConfigChanged() {}
}

/** Nivel gratis: sin servidor. */
object NoRemoteManagement : RemoteManagement {
    override val isLinked: Boolean = false
}
