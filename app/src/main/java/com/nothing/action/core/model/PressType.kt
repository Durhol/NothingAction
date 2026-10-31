package com.nothing.action.core.model

enum class PressType(val title: String, val badge: String, val animationDesc: String) {
    SINGLE("SINGLE PRESS", "QUICK", "1x Quick Inward Bezel Indent"),
    DOUBLE("DOUBLE PRESS", "PULSE", "2x Rapid Elastic Pulses"),
    LONG("LONG PRESS", "HOLD", "Tension Stretch & Deep Indent Hold")
}
