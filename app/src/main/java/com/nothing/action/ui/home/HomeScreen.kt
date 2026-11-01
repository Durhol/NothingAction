package com.nothing.action.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.action.core.model.PressType
import com.nothing.action.ui.components.*
import com.nothing.action.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            // Clean centered title matching Glyphify's iconic top layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                DotMatrixHeader(
                    text = "NOTHING (ACTION)",
                    fontSize = 24.sp,
                    showRedDot = true
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Free-standing Phone Hero Visualizer (Exactly like Glyphify)
            item {
                DeviceVisualizerHero(
                    deviceInfo = uiState.deviceInfo,
                    isServiceRunning = uiState.settings.isServiceEnabled,
                    colorTheme = uiState.settings.deviceColorTheme,
                    onTestGlyphPulse = { viewModel.testGlyphPulse() }
                )
            }

            // Master Engine Status Card (Inspired by Glyphify's "Extential" card)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (uiState.settings.isServiceEnabled) "Engine Active" else "Engine Paused",
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (uiState.settings.isServiceEnabled)
                                        "Listening to hardware triggers in background"
                                    else
                                        "Essential key listener is offline",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Switch(
                                checked = uiState.settings.isServiceEnabled,
                                onCheckedChange = { viewModel.toggleService() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = NothingRed,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF222222)
                                )
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CardBorder)
                        )

                        // 3 Sub-columns status (Matching Glyphify's bottom info section)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "TRIGGER KEY", color = TextMuted, fontSize = 10.sp, fontFamily = NdotFont)
                                Text(
                                    text = uiState.settings.targetKeyName,
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontFamily = NdotFont,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "BEZEL BUMP", color = TextMuted, fontSize = 10.sp, fontFamily = NdotFont)
                                Text(
                                    text = if (uiState.settings.isBezelAnimationEnabled) "ACTIVE // ${uiState.settings.bezelSide}" else "DISABLED",
                                    color = if (uiState.settings.isBezelAnimationEnabled) Color(0xFF4CAF50) else TextMuted,
                                    fontSize = 12.sp,
                                    fontFamily = NdotFont,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.8f)
                                    .clickable { onNavigateToSettings() },
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(text = "TUNING", color = TextMuted, fontSize = 10.sp, fontFamily = NdotFont)
                                Text(
                                    text = "CALIBRATE →",
                                    color = NothingRed,
                                    fontSize = 12.sp,
                                    fontFamily = NdotFont,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Bento Grid Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "HARDWARE TRIGGER ASSIGNMENTS",
                        color = TextWhite,
                        fontFamily = NdotFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Single Press Bento Card
            item {
                BentoCard(
                    pressType = PressType.SINGLE,
                    currentAction = uiState.settings.singleAction,
                    customAppPackage = uiState.settings.singleCustomPackage,
                    onClick = { viewModel.openActionPicker(PressType.SINGLE) }
                )
            }

            // Double Press Bento Card
            item {
                BentoCard(
                    pressType = PressType.DOUBLE,
                    currentAction = uiState.settings.doubleAction,
                    customAppPackage = uiState.settings.doubleCustomPackage,
                    onClick = { viewModel.openActionPicker(PressType.DOUBLE) }
                )
            }

            // Long Press Bento Card
            item {
                BentoCard(
                    pressType = PressType.LONG,
                    currentAction = uiState.settings.longAction,
                    customAppPackage = uiState.settings.longCustomPackage,
                    onClick = { viewModel.openActionPicker(PressType.LONG) }
                )
            }

            // Edge Bezel Button Animation & Calibration Tile
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BEZEL BUTTON BUMP (iOS 18 STYLE)",
                                    color = TextWhite,
                                    fontFamily = NdotFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Tactile mechanical screen bump on Essential key press • Pure OLED Black",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Switch(
                                checked = uiState.settings.isBezelAnimationEnabled,
                                onCheckedChange = { viewModel.toggleBezelAnimation() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = NothingRed,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF222222)
                                )
                            )
                        }

                        // Test & Calibrate quick buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.testBezelBend() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E1E1E),
                                    contentColor = TextWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "TEST ANIMATION", fontFamily = NdotFont, fontSize = 11.sp)
                            }

                            Button(
                                onClick = onNavigateToSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E1E1E),
                                    contentColor = TextWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = NothingRed
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "CALIBRATE", fontFamily = NdotFont, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Bento Grid: Settings and About (EXACTLY like Glyphify Image 2, perfectly equal in size)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Settings Bento Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                            .clickable { onNavigateToSettings() }
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Settings",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // About Bento Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                            .clickable { onNavigateToAbout() }
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "About",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "v1.0.2",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About",
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Action Picker Dialog
    uiState.activeDialogPressType?.let { pressType ->
        val currentAction = when (pressType) {
            PressType.SINGLE -> uiState.settings.singleAction
            PressType.DOUBLE -> uiState.settings.doubleAction
            PressType.LONG -> uiState.settings.longAction
        }

        ActionPickerDialog(
            pressType = pressType,
            selectedAction = currentAction,
            onActionSelected = { action ->
                viewModel.onActionSelected(pressType, action)
            },
            onDismissRequest = { viewModel.closeActionPicker() }
        )
    }

    // App Picker Dialog
    if (uiState.isAppPickerVisible) {
        AppPickerDialog(
            onAppSelected = { packageName ->
                viewModel.onAppSelected(packageName)
            },
            onDismissRequest = { viewModel.closeAppPicker() }
        )
    }
}
