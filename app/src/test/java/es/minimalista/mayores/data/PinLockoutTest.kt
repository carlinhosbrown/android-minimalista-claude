package es.minimalista.mayores.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PinLockoutTest {
    @Test fun freeAttemptsThenDoubling() {
        assertEquals(0, PinLockout.waitSecondsAfter(4))
        assertEquals(30, PinLockout.waitSecondsAfter(5))
        assertEquals(60, PinLockout.waitSecondsAfter(6))
        assertEquals(120, PinLockout.waitSecondsAfter(7))
        assertEquals(3600, PinLockout.waitSecondsAfter(50))
    }
}
