package com.nothing.action.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nothing.action.R

val NdotFont = FontFamily(
    Font(R.font.ndot57_caps, FontWeight.Bold),
    Font(R.font.ndot57_caps, FontWeight.Normal)
)

val NdotRegularFont = FontFamily(
    Font(R.font.ndot57, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NdotFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 2.5.sp,
        color = TextWhite
    ),
    displayMedium = TextStyle(
        fontFamily = NdotFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 2.sp,
        color = TextWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = NdotFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 1.5.sp,
        color = TextWhite
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp,
        color = TextWhite
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        letterSpacing = 0.5.sp,
        color = TextWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
        color = TextMuted
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = TextMuted
    ),
    labelSmall = TextStyle(
        fontFamily = NdotFont,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
        color = TextMuted
    )
)
