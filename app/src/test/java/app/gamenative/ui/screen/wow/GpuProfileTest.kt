package app.gamenative.ui.screen.wow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpuProfileTest {

    @Test
    fun questThreeAndThreeSAreDetected() {
        assertTrue(isMetaQuest3("Oculus", "Quest 3", "SXR2230P"))
        assertTrue(isMetaQuest3("Oculus", "Quest 3S", "SXR2230P"))
        assertTrue(isMetaQuest3("oculus", "Eureka", "SXR2230P"))
    }

    @Test
    fun otherDevicesAreNot() {
        assertFalse(isMetaQuest3("Oculus", "Quest 2", "SXR2130"))
        assertFalse(isMetaQuest3("AYN", "Odin2", "SM8550"))
        assertFalse(isMetaQuest3("samsung", "SM-S911B", "SM8550"))
    }

    @Test
    fun questUsesTheHeadsetsOwnDriverAndLighterDefaults() {
        assertEquals("System", GpuProfile.QUEST.driver)
        assertEquals("1280x720", GpuProfile.QUEST.screenSize)
        assertEquals(
            "version=System,adrenotoolsTurnip=1,resourceType=buffer,bcnEmulation=auto,quality=high,maxDeviceMemory=1536",
            graphicsDriverConfig(GpuProfile.QUEST),
        )
    }

    @Test
    fun otherDevicesKeepTheirGraphicsConfig() {
        assertEquals(
            "version=Turnip-WoW-scheduler-test,adrenotoolsTurnip=1,resourceType=buffer,bcnEmulation=auto,quality=high",
            graphicsDriverConfig(GpuProfile.ADRENO_740),
        )
    }

    @Test
    fun everyAdreno8xxGetsTheAdreno8xxProfile() {
        // Snapdragon 8s Gen 4 (SM8735, Adreno 825), issue #5.
        assertTrue(isAdreno8xx("Adreno (TM) 825", "SM8735", "qcom"))
        assertTrue(isAdreno8xx("", "SM8735", "qcom"))
        // Snapdragon 8 Elite and 8 Elite Gen 5, as before.
        assertTrue(isAdreno8xx("Adreno (TM) 830", "", "qcom"))
        assertTrue(isAdreno8xx("", "SM8850", "qcom"))
        // Snapdragon 8 Gen 5 (SM8845, Adreno 829), found by upstream.
        assertTrue(isAdreno8xx("", "SM8845", "qcom"))
        assertTrue(isAdreno8xx("", "", "sun"))
    }

    @Test
    fun adreno7xxIsNot8xx() {
        assertFalse(isAdreno8xx("Adreno (TM) 740", "SM8550", "kalama"))
        assertFalse(isAdreno8xx("Adreno (TM) 750", "SM8650", "pineapple"))
        assertFalse(isAdreno8xx("Adreno (TM) 735", "SM7675", "qcom"))
    }

    @Test
    fun exynos2400IsXclipse() {
        assertTrue(isXclipse940("s5e9945", "s5e9945", "s5e9945"))
        assertTrue(isXclipse940("", "s5e9945", ""))
        assertFalse(isXclipse940("SM8650", "pineapple", "pineapple"))
        assertFalse(isXclipse940("s5e9925", "s5e9925", "s5e9925"))
    }

    @Test
    fun xclipseUsesRadvWithoutBcnEmulation() {
        assertEquals(
            "version=RADV-Xclipse-xd-4f88558,adrenotoolsTurnip=1,resourceType=buffer,bcnEmulation=none,quality=high",
            graphicsDriverConfig(GpuProfile.XCLIPSE),
        )
    }
}
