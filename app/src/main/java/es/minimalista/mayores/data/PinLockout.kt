package es.minimalista.mayores.data

/**
 * Tras varios fallos seguidos, obliga a esperar antes de volver a probar el PIN.
 * 5 fallos = 30 s, y cada fallo más duplica la espera (máximo 1 hora).
 */
object PinLockout {
    const val FREE_ATTEMPTS = 5
    private const val BASE_SECONDS = 30L
    private const val MAX_SECONDS = 3600L

    fun waitSecondsAfter(failedAttempts: Int): Long {
        if (failedAttempts < FREE_ATTEMPTS) return 0
        val extra = (failedAttempts - FREE_ATTEMPTS).coerceAtMost(10)
        return (BASE_SECONDS shl extra).coerceAtMost(MAX_SECONDS)
    }
}
