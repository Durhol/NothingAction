package com.nothing.action.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ActionType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    TORCH(
        title = "Flashlight Toggle",
        description = "Instant rear LED torch on/off",
        icon = Icons.Default.FlashOn
    ),
    CAMERA(
        title = "Open Camera",
        description = "Launch camera viewfinder immediately",
        icon = Icons.Default.PhotoCamera
    ),
    GLYPH(
        title = "Glyph Light Pulse",
        description = "Trigger animated Glyph Interface matrix",
        icon = Icons.Default.Grain
    ),
    GEMINI(
        title = "Voice Assistant",
        description = "Launch voice assistant listening prompt",
        icon = Icons.Default.AutoAwesome
    ),
    SCREENSHOT(
        title = "Capture Screenshot",
        description = "Perform screen capture without button combo",
        icon = Icons.Default.Screenshot
    ),
    APP_LAUNCH(
        title = "Launch Custom App",
        description = "Open your favorite installed app",
        icon = Icons.Default.Apps
    ),
    NONE(
        title = "None (Disabled)",
        description = "Do not trigger any action",
        icon = Icons.Default.Block
    )
}
