package com.nothing.action.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nothing.action.R
import com.nothing.action.core.model.DeviceDetector
import com.nothing.action.core.model.DeviceInfo

/**
 * Free-standing authentic Nothing Phone back visualizer using official Glyphify assets.
 * Spans smoothly across the top of the display and blends into the OLED black background.
 */
@Composable
fun DeviceVisualizerHero(
    deviceInfo: DeviceInfo,
    isServiceRunning: Boolean,
    colorTheme: String,
    onTestGlyphPulse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveColor = if (colorTheme.equals("AUTO", ignoreCase = true)) {
        remember { DeviceDetector.getSystemDeviceColor() }
    } else {
        colorTheme.uppercase()
    }

    val imageRes = when {
        // Nothing Phone (3a) / (3a) Pro
        deviceInfo.name.contains("3A", ignoreCase = true) || deviceInfo.modelCode.contains("A059", ignoreCase = true) -> {
            when (effectiveColor) {
                "WHITE" -> R.drawable.phone3a_white
                "BLUE" -> R.drawable.phone3a_blue
                else -> R.drawable.phone3a_black
            }
        }
        // Nothing Phone (2a) / (2a) Plus
        deviceInfo.name.contains("2A", ignoreCase = true) || deviceInfo.modelCode.contains("A142", ignoreCase = true) -> {
            when (effectiveColor) {
                "WHITE" -> R.drawable.phone2a_white
                "BLUE" -> R.drawable.phone2a_blue
                else -> R.drawable.phone2a_black
            }
        }
        // Nothing Phone (2)
        deviceInfo.name.contains("PHONE (2)", ignoreCase = true) || deviceInfo.modelCode.contains("A065", ignoreCase = true) -> {
            if (effectiveColor == "WHITE") R.drawable.phone2_white else R.drawable.phone2_gray
        }
        // Nothing Phone (1)
        deviceInfo.name.contains("PHONE (1)", ignoreCase = true) || deviceInfo.modelCode.contains("A063", ignoreCase = true) -> {
            if (effectiveColor == "WHITE") R.drawable.phone1_white else R.drawable.phone1_black
        }
        // Nothing Phone (3)
        deviceInfo.name.contains("PHONE (3)", ignoreCase = true) || deviceInfo.modelCode.contains("A024", ignoreCase = true) -> {
            if (effectiveColor == "WHITE") R.drawable.phone3_white else R.drawable.phone3_black
        }
        else -> {
            if (effectiveColor == "WHITE") R.drawable.phone3a_white else R.drawable.phone3a_black
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTestGlyphPulse() },
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = deviceInfo.name,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .padding(horizontal = 24.dp)
        )
    }
}
