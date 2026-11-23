package es.minimalista.mayores.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Guarda el PIN familiar como hash PBKDF2 con sal, nunca en claro.
 * Es Kotlin puro para poder probarlo sin dispositivo.
 */
object PinHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 8

    fun isValidFormat(pin: String): Boolean =
        pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it in '0'..'9' }

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(pin: String, salt: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), Base64.getDecoder().decode(salt), ITERATIONS, KEY_BITS)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.getEncoder().encodeToString(key)
    }

    fun matches(pin: String, salt: String, expectedHash: String): Boolean {
        val actual = Base64.getDecoder().decode(hash(pin, salt))
        val expected = Base64.getDecoder().decode(expectedHash)
        return MessageDigest.isEqual(actual, expected)
    }
}
