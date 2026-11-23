package es.minimalista.mayores.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    @Test fun correctPinMatches() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertTrue(PinHasher.matches("1234", salt, hash))
        assertFalse(PinHasher.matches("1235", salt, hash))
    }

    @Test fun sameSaltSameHashDifferentSaltDifferentHash() {
        val salt = PinHasher.newSalt()
        assertEquals(PinHasher.hash("0000", salt), PinHasher.hash("0000", salt))
        assertNotEquals(PinHasher.hash("0000", salt), PinHasher.hash("0000", PinHasher.newSalt()))
    }

    @Test fun formatRules() {
        assertTrue(PinHasher.isValidFormat("1234"))
        assertTrue(PinHasher.isValidFormat("12345678"))
        assertFalse(PinHasher.isValidFormat("123"))
        assertFalse(PinHasher.isValidFormat("123456789"))
        assertFalse(PinHasher.isValidFormat("12a4"))
    }
}
