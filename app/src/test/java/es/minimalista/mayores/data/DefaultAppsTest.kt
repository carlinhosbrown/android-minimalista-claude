package es.minimalista.mayores.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DefaultAppsTest {
    @Test fun xiaomiPhone() {
        val installed = setOf(
            "com.android.contacts", "com.whatsapp", "com.miui.gallery", "com.android.camera",
            "com.google.android.apps.maps", "com.google.android.calendar", "com.android.mms",
            "com.instagram.android",
        )
        assertEquals(
            listOf(
                "com.android.contacts", "com.whatsapp", "com.miui.gallery", "com.android.camera",
                "com.google.android.apps.maps", "com.google.android.calendar", "com.instagram.android",
            ),
            DefaultApps.pickDefaults(installed),
        )
    }

    @Test fun systemDefaultsWinAndSmsIsNeverPicked() {
        val installed = setOf(
            "com.samsung.android.dialer", "com.google.android.dialer", "com.sec.android.app.camera",
            "com.samsung.android.messaging",
        )
        val picked = DefaultApps.pickDefaults(installed, preferredDialer = "com.samsung.android.dialer")
        assertEquals(listOf("com.samsung.android.dialer", "com.sec.android.app.camera"), picked)
        assertFalse(picked.any { it in DefaultApps.notRecommended })
    }
}
