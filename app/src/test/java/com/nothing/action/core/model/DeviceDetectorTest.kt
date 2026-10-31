package com.nothing.action.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DeviceDetectorTest {

    @Test
    fun testDeviceInfoDefaults() {
        val info = DeviceDetector.getDeviceInfo()
        assertNotNull(info)
        assertNotNull(info.name)
        assertNotNull(info.modelCode)
    }
}
