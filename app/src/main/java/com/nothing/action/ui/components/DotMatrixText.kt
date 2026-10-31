package com.nothing.action.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.action.ui.theme.NdotFont
import com.nothing.action.ui.theme.NothingRed

@Composable
fun DotMatrixHeader(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    showRedDot: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            color = Color.White,
            fontSize = fontSize,
            fontFamily = NdotFont,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.5.sp
        )
        if (showRedDot) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(fontSize.value.dp * 0.22f)
                    .clip(CircleShape)
                    .background(NothingRed)
            )
        }
    }
}
