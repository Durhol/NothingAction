package com.nothing.action.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.action.core.model.ActionType
import com.nothing.action.core.model.PressType
import com.nothing.action.ui.theme.*

@Composable
fun BentoCard(
    pressType: PressType,
    currentAction: ActionType,
    customAppPackage: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displaySubtitle = if (currentAction == ActionType.APP_LAUNCH && customAppPackage.isNotBlank()) {
        "Custom Package: $customAppPackage"
    } else {
        pressType.animationDesc
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceDark)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular action icon container
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF191919))
                    .border(1.dp, Color(0xFF282828), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentAction.icon,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pressType.title,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = NdotFont,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${pressType.badge}",
                        color = if (pressType == PressType.DOUBLE) NothingRed else TextWhite,
                        fontSize = 10.sp,
                        fontFamily = NdotFont,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentAction.title,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = displaySubtitle,
                    color = TextMuted.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF555555)
            )
        }
    }
}
