package com.nothing.action.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.nothing.action.core.data.ActionPreferencesRepository
import com.nothing.action.core.model.PressType
import com.nothing.action.glyph.NothingGlyphController
import com.nothing.action.overlay.BezelBendOverlayManager
import com.nothing.action.service.NothingAccessibilityService
import com.nothing.action.ui.components.DotMatrixHeader
import com.nothing.action.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onRerunOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { ActionPreferencesRepository(context) }
    val settings by repository.settingsFlow.collectAsState(initial = null)

    val overlayManager = remember { BezelBendOverlayManager(context) }
    val glyphController = remember { NothingGlyphController(context).apply { bind() } }

    var isAccessibilityGranted by remember { mutableStateOf(false) }
    var isOverlayGranted by remember { mutableStateOf(false) }
    var isBatteryExempted by remember { mutableStateOf(false) }
    var showKeyCalibrationDialog by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        isOverlayGranted = Settings.canDrawOverlays(context)
        isAccessibilityGranted = isAccessibilityServiceEnabled(context)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryExempted = pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    DisposableEffect(Unit) {
        onDispose {
            overlayManager.detach()
            glyphController.unbind()
        }
    }

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

                DotMatrixHeader(text = "SETTINGS", fontSize = 20.sp)
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
            // Section 1: Hardware Trigger Key
            item {
                Text(
                    text = "HARDWARE TRIGGER CALIBRATION",
                    color = NothingRed,
                    fontFamily = NdotFont,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE KEY: ${settings?.targetKeyName ?: "ESSENTIAL KEY"}",
                                color = TextWhite,
                                fontFamily = NdotFont,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "ScanCode #${settings?.targetScanCode ?: 250} • KeyCode #${settings?.targetKeyCode ?: 0}",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { showKeyCalibrationDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NothingRed,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "DETECT", fontFamily = NdotFont, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Community guide for remapping/disabling Essential Key by z3phydev
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF161616))
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/z3phydev/How-to-remap-or-disable-the-Essential-Key")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "REMAP / DISABLE ESSENTIAL KEY GUIDE",
                            color = TextWhite,
                            fontFamily = NdotFont,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "github.com/z3phydev/How-to-remap-or-disable-the-Essential-Key",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Section 2: iOS 18-Style Bezel Button Bump Calibration
            item {
                Text(
                    text = "BEZEL BUTTON BUMP CALIBRATION",
                    color = NothingRed,
                    fontFamily = NdotFont,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            item {
                val currentSide = settings?.bezelSide ?: "RIGHT"
                val currentYPercent = settings?.bezelYPercent ?: 0.38f
                val currentButtonLength = settings?.bezelButtonLength ?: 95f
                val currentButtonWidth = settings?.bezelButtonWidth ?: 20f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "BUTTON GEOMETRY & POSITION",
                            color = TextWhite,
                            fontFamily = NdotFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Side Selector (RIGHT vs LEFT)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isRight = currentSide.equals("RIGHT", ignoreCase = true)

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.setBezelCalibration("RIGHT", currentYPercent, currentButtonLength, currentButtonWidth)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRight) NothingRed else Color(0xFF1E1E1E),
                                    contentColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "RIGHT BEZEL", fontFamily = NdotFont, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.setBezelCalibration("LEFT", currentYPercent, currentButtonLength, currentButtonWidth)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isRight) NothingRed else Color(0xFF1E1E1E),
                                    contentColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "LEFT BEZEL", fontFamily = NdotFont, fontSize = 11.sp)
                            }
                        }

                        // Vertical position slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "VERTICAL ALIGNMENT", color = TextMuted, fontSize = 11.sp, fontFamily = NdotFont)
                                Text(
                                    text = "${(currentYPercent * 100).toInt()}% HEIGHT",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontFamily = NdotFont
                                )
                            }
                            Slider(
                                value = currentYPercent,
                                onValueChange = {
                                    coroutineScope.launch {
                                        repository.setBezelCalibration(currentSide, it, currentButtonLength, currentButtonWidth)
                                    }
                                },
                                valueRange = 0.15f..0.85f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NothingRed,
                                    activeTrackColor = NothingRed,
                                    inactiveTrackColor = Color(0xFF2E2E2E)
                                )
                            )
                        }

                        // Button Length (Height) slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "BUTTON LENGTH (HEIGHT)", color = TextMuted, fontSize = 11.sp, fontFamily = NdotFont)
                                Text(
                                    text = "${currentButtonLength.toInt()} DP",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontFamily = NdotFont
                                )
                            }
                            Slider(
                                value = currentButtonLength,
                                onValueChange = {
                                    coroutineScope.launch {
                                        repository.setBezelCalibration(currentSide, currentYPercent, it, currentButtonWidth)
                                    }
                                },
                                valueRange = 50f..200f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NothingRed,
                                    activeTrackColor = NothingRed,
                                    inactiveTrackColor = Color(0xFF2E2E2E)
                                )
                            )
                        }

                        // Button Protrusion Width (Depth) slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "BUMP PROTRUSION WIDTH", color = TextMuted, fontSize = 11.sp, fontFamily = NdotFont)
                                Text(
                                    text = "${currentButtonWidth.toInt()} DP",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontFamily = NdotFont
                                )
                            }
                            Slider(
                                value = currentButtonWidth,
                                onValueChange = {
                                    coroutineScope.launch {
                                        repository.setBezelCalibration(currentSide, currentYPercent, currentButtonLength, it)
                                    }
                                },
                                valueRange = 8f..45f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NothingRed,
                                    activeTrackColor = NothingRed,
                                    inactiveTrackColor = Color(0xFF2E2E2E)
                                )
                            )
                        }

                        // Test Bezel Bend Button
                        Button(
                            onClick = {
                                overlayManager.updateCalibration(currentSide, currentYPercent, currentButtonLength, currentButtonWidth)
                                overlayManager.triggerBezelAnimation(PressType.DOUBLE)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF242424),
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "TEST BEZEL BUTTON ANIMATION", fontFamily = NdotFont, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            // Section 3: Glyph Interface Settings
            item {
                Text(
                    text = "NOTHING GLYPH INTERFACE",
                    color = NothingRed,
                    fontFamily = NdotFont,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GLYPH LIGHT PULSE ON CLICK",
                                    color = TextWhite,
                                    fontFamily = NdotFont,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Fires physical rear LED Glyph pulse when Essential Key is pressed",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Switch(
                                checked = settings?.glyphFeedbackEnabled ?: true,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        repository.setGlyphFeedbackEnabled(it)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = NothingRed,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF222222)
                                )
                            )
                        }

                        Button(
                            onClick = {
                                glyphController.triggerPulse(cycles = 2, maxBrightness = 4000)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF242424),
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "PULSE PHYSICAL GLYPH LEDS NOW", fontFamily = NdotFont, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            // Section 4: System Permissions Checklist
            item {
                Text(
                    text = "SYSTEM PERMISSIONS CHECKLIST",
                    color = NothingRed,
                    fontFamily = NdotFont,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // 1. Accessibility Service
            item {
                PermissionItem(
                    title = "ACCESSIBILITY SERVICE",
                    subtitle = "Required to intercept hardware button clicks and capture screenshots",
                    isGranted = isAccessibilityGranted,
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // 2. Display Overlay
            item {
                PermissionItem(
                    title = "DISPLAY OVERLAY (BEZEL BEND)",
                    subtitle = "Required to render the physical bezel button bump animation",
                    isGranted = isOverlayGranted,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // 3. Battery Optimization Exemption
            item {
                PermissionItem(
                    title = "DISABLE BATTERY OPTIMIZATION",
                    subtitle = "Prevents Android from killing the background service in Doze mode",
                    isGranted = isBatteryExempted,
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val generalIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(generalIntent)
                        }
                    }
                )
            }

            // Rerun setup wizard
            item {
                Button(
                    onClick = onRerunOnboarding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E1E1E),
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Text(text = "RE-RUN INITIAL SETUP WIZARD", fontFamily = NdotFont, letterSpacing = 1.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Key Calibration Live Modal Dialog
    if (showKeyCalibrationDialog) {
        KeyCalibrationModalDialog(
            currentKey = settings?.targetKeyName ?: "ESSENTIAL KEY",
            onKeySelected = { code, scan, name ->
                coroutineScope.launch {
                    repository.setTargetKey(code, scan, name)
                }
                showKeyCalibrationDialog = false
            },
            onDismiss = { showKeyCalibrationDialog = false }
        )
    }
}

@Composable
fun KeyCalibrationModalDialog(
    currentKey: String,
    onKeySelected: (Int, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var lastPressedCode by remember { mutableIntStateOf(-1) }
    var lastPressedScanCode by remember { mutableIntStateOf(-1) }
    var lastPressedName by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        NothingAccessibilityService.isCalibratingKey = true
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == NothingAccessibilityService.ACTION_KEY_CALIBRATION_EVENT) {
                    val code = intent.getIntExtra(NothingAccessibilityService.EXTRA_KEY_CODE, 0)
                    val scan = intent.getIntExtra(NothingAccessibilityService.EXTRA_KEY_SCAN_CODE, 0)
                    val name = intent.getStringExtra(NothingAccessibilityService.EXTRA_KEY_NAME)
                        ?: if (scan == 250) "ESSENTIAL KEY" else "KEY (SCAN $scan)"

                    lastPressedCode = code
                    lastPressedScanCode = if (scan > 0) scan else if (code > 0) 0 else 250
                    lastPressedName = name
                }
            }
        }
        val filter = IntentFilter(NothingAccessibilityService.ACTION_KEY_CALIBRATION_EVENT)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        onDispose {
            NothingAccessibilityService.isCalibratingKey = false
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "HARDWARE BUTTON DETECTOR",
                    color = NothingRed,
                    fontFamily = NdotFont,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "PRESS ESSENTIAL KEY",
                    color = TextWhite,
                    fontFamily = NdotFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(if (lastPressedScanCode > 0 || lastPressedCode > 0) Color(0xFF1E3A24) else Color(0xFF181818))
                        .border(
                            2.dp,
                            if (lastPressedScanCode > 0 || lastPressedCode > 0) Color(0xFF4CAF50) else NothingRed,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (lastPressedScanCode > 0 || lastPressedCode > 0) Icons.Default.Check else Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = if (lastPressedScanCode > 0 || lastPressedCode > 0) Color(0xFF4CAF50) else TextWhite,
                        modifier = Modifier.size(38.dp)
                    )
                }

                if (lastPressedScanCode > 0 || lastPressedCode > 0) {
                    Text(
                        text = "DETECTED: $lastPressedName",
                        color = Color(0xFF4CAF50),
                        fontFamily = NdotFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { onKeySelected(lastPressedCode, lastPressedScanCode, lastPressedName) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "USE THIS BUTTON", fontFamily = NdotFont, letterSpacing = 1.sp)
                    }
                } else {
                    Text(
                        text = "Press your Essential key now... Current: $currentKey",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { onKeySelected(0, 250, "ESSENTIAL KEY") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222), contentColor = TextWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "USE ESSENTIAL KEY (SCANCODE 250)", fontSize = 11.sp, fontFamily = NdotFont)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "CANCEL", fontFamily = NdotFont)
                }
            }
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, if (isGranted) Color(0xFF2E4E32) else CardBorder, RoundedCornerShape(20.dp))
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
                    .background(if (isGranted) Color(0xFF1B2C1E) else Color(0xFF2C1919)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF4CAF50) else NothingRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontFamily = NdotFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGranted) "GRANTED" else "TAP TO GRANT ACCESS →",
                    color = if (isGranted) Color(0xFF4CAF50) else NothingRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NdotFont
                )
            }
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedServiceName = "${context.packageName}/com.nothing.action.service.NothingAccessibilityService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabledServices.split(":").any { it.equals(expectedServiceName, ignoreCase = true) }
}
