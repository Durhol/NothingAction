package com.nothing.action.core.model

import android.os.Build

data class DeviceInfo(
    val name: String,
    val codename: String,
    val modelCode: String,
    val manufacturer: String,
    val isNothingDevice: Boolean,
    val glyphLedsCount: Int,
    val hasEssentialKey: Boolean,
    val glyphType: GlyphHardwareType
)

enum class GlyphHardwareType {
    STRIP,
    MATRIX,
    NONE
}

object DeviceDetector {

    fun getDeviceInfo(): DeviceInfo {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val model = Build.MODEL.orEmpty().trim().uppercase()
        val product = Build.PRODUCT.orEmpty().trim().uppercase()

        val isNothing = manufacturer.contains("Nothing", ignoreCase = true) ||
                model.startsWith("A0") || model.startsWith("A1") ||
                model.contains("AIN065") || product.contains("ASTEROIDS") ||
                product.contains("PACMAN") || product.contains("PONG") || product.contains("SPACEWAR")

        val (name, codename, leds, glyphType) = when {
            // Nothing Phone (3a) & (3a) Plus / Pro (A059 / A059P)
            model.contains("A059P") || product.contains("ASTEROIDSPRO") -> {
                Quad("NOTHING PHONE (3a) PRO", "ASTEROIDS PRO", 36, GlyphHardwareType.STRIP)
            }
            model.contains("A059") || product.contains("ASTEROIDS") -> {
                Quad("NOTHING PHONE (3a)", "ASTEROIDS", 36, GlyphHardwareType.STRIP)
            }
            // Nothing Phone (3a) Lite (A001T)
            model.contains("A001T") || product.contains("GALAXIAN") -> {
                Quad("NOTHING PHONE (3a) LITE", "GALAXIAN", 26, GlyphHardwareType.STRIP)
            }
            // Nothing Phone (2a) & (2a) Plus (A142 / A142P)
            model.contains("A142P") || product.contains("PACMANPRO") -> {
                Quad("NOTHING PHONE (2a) PLUS", "PACMAN PRO", 26, GlyphHardwareType.STRIP)
            }
            model.contains("A142") || product.contains("PACMAN") -> {
                Quad("NOTHING PHONE (2a)", "PACMAN", 26, GlyphHardwareType.STRIP)
            }
            // Nothing Phone (2) (A065 / AIN065)
            model.contains("A065") || model.contains("AIN065") || product.contains("PONG") -> {
                Quad("NOTHING PHONE (2)", "PONG", 33, GlyphHardwareType.STRIP)
            }
            // Nothing Phone (1) (A063)
            model.contains("A063") || product.contains("SPACEWAR") -> {
                Quad("NOTHING PHONE (1)", "SPACEWAR", 15, GlyphHardwareType.STRIP)
            }
            // Nothing Phone (3) (A024)
            model.contains("A024") || product.contains("METROID") -> {
                Quad("NOTHING PHONE (3)", "METROID", 625, GlyphHardwareType.MATRIX)
            }
            // Nothing Phone (4a) series (A069 / A069P)
            model.contains("A069P") -> {
                Quad("NOTHING PHONE (4a) PRO", "TETRIS PRO", 169, GlyphHardwareType.MATRIX)
            }
            model.contains("A069") -> {
                Quad("NOTHING PHONE (4a)", "TETRIS", 6, GlyphHardwareType.STRIP)
            }
            // CMF Phone 1 & CMF Phone 2 Pro
            model.contains("A015") -> {
                Quad("CMF PHONE 1", "TETRIS", 0, GlyphHardwareType.NONE)
            }
            model.contains("A001") -> {
                Quad("CMF PHONE 2 PRO", "GALAGA", 0, GlyphHardwareType.NONE)
            }
            isNothing -> {
                Quad("NOTHING DEVICE ($model)", "NOTHING", 36, GlyphHardwareType.STRIP)
            }
            else -> {
                Quad("NOTHING PHONE (3a) // EMULATED", "ASTEROIDS", 36, GlyphHardwareType.STRIP)
            }
        }

        return DeviceInfo(
            name = name,
            codename = codename,
            modelCode = if (model.isNotBlank()) model else "A059",
            manufacturer = if (isNothing) "NOTHING TECHNOLOGY" else manufacturer.uppercase(),
            isNothingDevice = isNothing,
            glyphLedsCount = leds,
            hasEssentialKey = true,
            glyphType = glyphType
        )
    }

    /**
     * Reads system hardware color properties (e.g. ro.boot.hardware.color) on Nothing OS.
     */
    fun getSystemDeviceColor(): String {
        try {
            val sysPropClass = Class.forName("android.os.SystemProperties")
            val getMethod = sysPropClass.getMethod("get", String::class.java)
            val colorProp = (getMethod.invoke(null, "ro.boot.hardware.color") as? String)
                ?.takeIf { it.isNotBlank() }
                ?: (getMethod.invoke(null, "ro.boot.color") as? String)
                ?: ""

            if (colorProp.contains("white", ignoreCase = true) || colorProp.contains("milk", ignoreCase = true)) {
                return "WHITE"
            }
            if (colorProp.contains("blue", ignoreCase = true)) {
                return "BLUE"
            }
            if (colorProp.contains("black", ignoreCase = true) || colorProp.contains("dark", ignoreCase = true)) {
                return "DARK"
            }
        } catch (_: Exception) {}
        return "DARK"
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
