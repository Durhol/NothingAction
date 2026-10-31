package com.nothing.action.ui.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.action.ui.components.DotMatrixHeader
import com.nothing.action.ui.theme.*

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                DotMatrixHeader(text = "ABOUT", fontSize = 20.sp)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                        .padding(22.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NothingRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NOTHING OS ECOSYSTEM",
                                color = TextMuted,
                                fontFamily = NdotFont,
                                fontSize = 11.sp,
                                letterSpacing = 1.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "NOTHING ACTION",
                            color = TextWhite,
                            fontFamily = NdotFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "VERSION 1.0.0 // 2026",
                            color = NothingRed,
                            fontFamily = NdotFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Crafted specifically for devices equipped with the dedicated physical Essential Key and Action Button:\n\n• Nothing Phone (3a)\n• Nothing Phone (3a) Pro\n• Nothing Phone (3a) Lite\n• Nothing Phone (3)\n• Nothing Phone (4a) & (4a) Pro\n\nAlso fully compatible with Nothing Phone (1, 2, 2a) and Android smartphones with remappable hardware keys. Integrates authentic NDot 57 typography, iOS 18-style mechanical tactile bezel button animations, and direct Nothing Glyph Interface hardware synchronization.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "COMMUNITY & DEVELOPER",
                    color = NothingRed,
                    fontFamily = NdotFont,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Telegram Link to author: t.me/Tyxap
            item {
                AboutLinkItem(
                    title = "DEVELOPER TELEGRAM",
                    subtitle = "@Tyxap • Contact Developer",
                    icon = Icons.AutoMirrored.Filled.Send,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Tyxap")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Telegram: @Tyxap", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // GitHub Repository Link
            item {
                AboutLinkItem(
                    title = "GITHUB REPOSITORY",
                    subtitle = "github.com/Durhol/NothingAction",
                    icon = Icons.Default.Code,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Durhol/NothingAction")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "https://github.com/Durhol/NothingAction", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // Design System Note
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(PureBlack)
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "(NDOT) DESIGN SYSTEM\nCrafted with monochrome aesthetics, organic curves, real hardware photo renders, and true OLED black contrast.",
                        color = Color(0xFF777777),
                        fontFamily = NdotFont,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun AboutLinkItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontFamily = NdotFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = Color(0xFF555555),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
