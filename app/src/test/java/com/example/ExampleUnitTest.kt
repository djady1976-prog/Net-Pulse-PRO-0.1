package com.example

import com.example.data.DeviceCategory
import com.example.data.TrustStatus
import com.example.ui.DeviceSortOption
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.EnglishStrings
import com.example.ui.i18n.GermanStrings
import com.example.ui.i18n.ItalianStrings
import com.example.ui.i18n.RomanianStrings
import com.example.ui.i18n.toAppStrings
import com.example.ui.theme.AppThemePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun allFourLanguages_haveNonEmptyTranslationsAndParameterizedStrings() {
        val languages = listOf(
            AppLanguage.RO to RomanianStrings,
            AppLanguage.EN to EnglishStrings,
            AppLanguage.IT to ItalianStrings,
            AppLanguage.DE to GermanStrings
        )

        assertEquals(4, AppLanguage.entries.size)

        for ((lang, strings) in languages) {
            assertEquals(strings, lang.toAppStrings())

            // Verify every String property of AppStrings is non-blank via reflection
            val properties = AppStrings::class.java.declaredFields
                .filter { it.type == String::class.java }
            assertTrue("Expected at least 140 translation keys", properties.size >= 140)

            for (field in properties) {
                field.isAccessible = true
                val value = field.get(strings) as? String
                assertFalse(
                    "Language ${lang.code} has blank value for key '${field.name}'",
                    value.isNullOrBlank()
                )
            }

            // Verify parameterized translation lambdas and enum helpers
            assertFalse(strings.intrusionBannerTitle(3).isBlank())
            assertFalse(strings.intrusionBannerSuspiciousBody(2).isBlank())
            assertFalse(strings.connectedDevicesHeader(8).isBlank())
            assertFalse(strings.watchdogUptimeTitle("99.9%").isBlank())
            assertFalse(strings.openPortsHeader(4).isBlank())
            assertFalse(strings.securitySummaryBody(5, 2).isBlank())
            assertFalse(strings.hiddenCameraDetectorSub(1).isBlank())
            assertFalse(strings.quarantineQueueHeader(2).isBlank())
            assertFalse(strings.enterPinLabel("1234").isBlank())
            assertFalse(strings.securedLogSub(12).isBlank())

            for (cat in DeviceCategory.entries) {
                assertFalse(strings.categoryName(cat).isBlank())
            }
            for (trust in TrustStatus.entries) {
                assertFalse(strings.trustName(trust).isBlank())
            }
            for (sort in DeviceSortOption.entries) {
                assertFalse(strings.sortOptionName(sort).isBlank())
            }
            assertFalse(strings.riskLevelName("CRITIC").isBlank())
            assertFalse(strings.severityName("AVERTISMENT").isBlank())
        }
    }

    @Test
    fun specificLanguageTerminology_isAccurateAcrossAllFourLanguages() {
        // Romanian (RO)
        assertEquals("Scaner", RomanianStrings.navRadar)
        assertEquals("Trafic", RomanianStrings.navTraffic)
        assertEquals("Unelte Pro", RomanianStrings.navTools)
        assertEquals("Securitate", RomanianStrings.navAlerts)
        assertEquals("Istoric", RomanianStrings.navHistory)
        assertEquals("Scanează Subrețeaua LAN", RomanianStrings.scanNetworkBtn)

        // English (EN)
        assertEquals("Scanner", EnglishStrings.navRadar)
        assertEquals("Traffic", EnglishStrings.navTraffic)
        assertEquals("Pro Tools", EnglishStrings.navTools)
        assertEquals("Security", EnglishStrings.navAlerts)
        assertEquals("History", EnglishStrings.navHistory)
        assertEquals("Scan LAN Subnet", EnglishStrings.scanNetworkBtn)

        // Italian (IT)
        assertEquals("Scanner", ItalianStrings.navRadar)
        assertEquals("Traffico", ItalianStrings.navTraffic)
        assertEquals("Strumenti", ItalianStrings.navTools)
        assertEquals("Sicurezza", ItalianStrings.navAlerts)
        assertEquals("Cronologia", ItalianStrings.navHistory)
        assertEquals("Scansiona Sottorete LAN", ItalianStrings.scanNetworkBtn)

        // German (DE)
        assertEquals("Scanner", GermanStrings.navRadar)
        assertEquals("Verkehr", GermanStrings.navTraffic)
        assertEquals("Pro-Tools", GermanStrings.navTools)
        assertEquals("Sicherheit", GermanStrings.navAlerts)
        assertEquals("Verlauf", GermanStrings.navHistory)
        assertEquals("LAN-Subnetz scannen", GermanStrings.scanNetworkBtn)
    }
}
