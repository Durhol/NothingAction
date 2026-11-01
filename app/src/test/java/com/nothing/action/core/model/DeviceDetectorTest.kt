package com.nothing.action.core.model

import com.nothing.action.core.data.ActionSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceDetectorTest {

    @Test
    fun testDeviceInfoDefaults() {
        val info = DeviceDetector.getDeviceInfo()
        assertNotNull(info)
        assertNotNull(info.name)
        assertNotNull(info.modelCode)
        assertNotNull(info.codename)
    }

    @Test
    fun testActionSettingsDefaults() {
        val settings = ActionSettings()
        assertEquals(250, settings.targetScanCode)
        assertEquals("ESSENTIAL KEY", settings.targetKeyName)
        assertEquals(12f, settings.bezelCornerRadius, 0.01f)
        assertEquals("#000000", settings.bezelFillColor)
        assertFalse(settings.bezelStrokeEnabled)
        assertEquals(1.5f, settings.bezelStrokeWidth, 0.01f)
        assertEquals("#333333", settings.bezelStrokeColor)
        assertEquals(95f, settings.bezelButtonLength, 0.01f)
        assertEquals(22f, settings.bezelButtonWidth, 0.01f)
        assertTrue(settings.isBezelAnimationEnabled)
    }
}
