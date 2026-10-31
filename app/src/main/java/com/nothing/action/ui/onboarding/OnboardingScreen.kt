package com.nothing.action.ui.onboarding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.core.content.ContextCompat
import com.nothing.action.core.data.ActionPreferencesRepository
import com.nothing.action.core.model.DeviceDetector
import com.nothing.action.service.NothingAccessibilityService
import com.nothing.action.ui.components.DotMatrixHeader
import com.nothing.action.ui.settings.isAccessibilityServiceEnabled
import com.nothing.action.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { ActionPreferencesRepository(context) }
    val deviceInfo = remember { DeviceDetector.getDeviceInfo() }

    var currentStep by remember { mutableIntStateOf(1) } // 1: Welcome, 2: Permissions, 3: Key Calibration

    // Permissions state
    var isAccessibilityGranted by remember { mutableStateOf(false) }
    var isOverlayGranted by remember { mutableStateOf(false) }
    var isBatteryExempted by remember { mutableStateOf(false) }

    // Key calibration state - DEFAULT TO ESSENTIAL KEY (SCANCODE 250)
    var detectedKeyCode by remember { mutableIntStateOf(0) }
    var detectedScanCode by remember { mutableIntStateOf(250) }
    var detectedKeyName by remember { mutableStateOf("ESSENTIAL KEY") }
    var isKeyRegistered by remember { mutableStateOf(true) }

    fun refreshPermissions() {
        isOverlayGranted = Settings.canDrawOverlays(context)
        isAccessibilityGranted = isAccessibilityServiceEnabled(context)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryExempted = pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    // Register broadcast receiver to receive key detection events from AccessibilityService & Activity
    DisposableEffect(currentStep) {
        if (currentStep == 3) {
            NothingAccessibilityService.isCalibratingKey = true
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    if (intent?.action == NothingAccessibilityService.ACTION_KEY_CALIBRATION_EVENT) {
                        val keyCode = intent.getIntExtra(NothingAccessibilityService.EXTRA_KEY_CODE, 0)
                        val scanCode = intent.getIntExtra(NothingAccessibilityService.EXTRA_KEY_SCAN_CODE, 0)
                        val keyName = intent.getStringExtra(NothingAccessibilityService.EXTRA_KEY_NAME)
                            ?: if (scanCode == 250) "ESSENTIAL KEY" else "KEY (SCAN $scanCode)"

                        detectedKeyCode = keyCode
                        detectedScanCode = if (scanCode > 0) scanCode else if (keyCode > 0) 0 else 250
                        detectedKeyName = keyName
                        isKeyRegistered = true
                    }
                }
            }
            val filter = IntentFilter(NothingAccessibilityService.ACTION_KEY_CALIBRATION_EVENT)
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            onDispose {
                NothingAccessibilityService.isCalibratingKey = false
                try {
                    context.unregisterReceiver(receiver)
                } catch (_: Exception) {}
            }
        } else {
            NothingAccessibilityService.isCalibratingKey = false
            onDispose { }
        }
    }

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DotMatrixHeader(text = "SETUP WIZARD", fontSize = 18.sp)

                Text(
                    text = "STEP $currentStep / 3",
                    color = NothingRed,
                    fontFamily = NdotFont,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 22.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "wizard_step",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    1 -> StepWelcome(deviceInfo = deviceInfo)
                    2 -> StepPermissions(
                        isAccessibilityGranted = isAccessibilityGranted,
                        isOverlayGranted = isOverlayGranted,
                        isBatteryExempted = isBatteryExempted,
                        onRefresh = { refreshPermissions() }
                    )
                    3 -> StepKeyCalibration(
                        detectedKeyCode = detectedKeyCode,
                        detectedScanCode = detectedScanCode,
                        detectedKeyName = detectedKeyName,
                        isKeyRegistered = isKeyRegistered,
                        onManualSelect = { code, scan, name ->
                            detectedKeyCode = code
                            detectedScanCode = scan
                            detectedKeyName = name
                            isKeyRegistered = true
                        }
                    )
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(text = "BACK", fontFamily = NdotFont, letterSpacing = 1.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            refreshPermissions()
                            currentStep++
                        } else {
                            // Finish setup
                            coroutineScope.launch {
                                repository.setTargetKey(detectedKeyCode, detectedScanCode, detectedKeyName)
                                repository.setOnboardingCompleted(true)
                                onFinished()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextWhite,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(
                        text = if (currentStep == 3) "FINISH & START →" else "CONTINUE →",
                        fontFamily = NdotFont,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StepWelcome(deviceInfo: com.nothing.action.core.model.DeviceInfo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "HARDWARE DETECTED",
            color = NothingRed,
            fontFamily = NdotFont,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp
        )

        Text(
            text = deviceInfo.name,
            color = TextWhite,
            fontFamily = NdotFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Text(
            text = "Essential Button Engine transforms your smartphone's dedicated Essential Key into a multi-action trigger with haptic iOS 18-style bezel button animation and real Glyph interface synchronization.",
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "DEVICE MODEL", color = TextMuted, fontSize = 11.sp, fontFamily = NdotFont)
                    Text(text = deviceInfo.modelCode, color = TextWhite, fontSize = 11.sp, fontFamily = NdotFont)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "CODENAME", color = TextMuted, fontSize = 11.sp, fontFamily = NdotFont)
                    Text(text = deviceInfo.codename, color = TextWhite, fontSize = 11.sp, fontFamily = NdotFont)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "GLYPH CHANNELS", color = TextMuted, fontSize = 11.sp, fontFamily = NdotFont)
                    Text(text = "${deviceInfo.glyphLedsCount} LEDS", color = NothingRed, fontSize = 11.sp, fontFamily = NdotFont)
                }
            }
        }
    }
}

@Composable
private fun StepPermissions(
    isAccessibilityGranted: Boolean,
    isOverlayGranted: Boolean,
    isBatteryExempted: Boolean,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "PERMISSIONS REQUIRED",
            color = NothingRed,
            fontFamily = NdotFont,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp
        )

        Text(
            text = "SYSTEM ACCESS",
            color = TextWhite,
            fontFamily = NdotFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Text(
            text = "To listen for physical Essential button clicks and draw the tactile bezel button animation, please grant the following permissions:",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 1. Accessibility Service
        WizardPermissionCard(
            title = "1. ACCESSIBILITY SERVICE",
            subtitle = "Listens for physical hardware Essential Key clicks",
            isGranted = isAccessibilityGranted,
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        )

        // 2. Display Overlay
        WizardPermissionCard(
            title = "2. DISPLAY OVERLAY",
            subtitle = "Draws tactile bezel button protrusion animation",
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

        // 3. Battery Exemption
        WizardPermissionCard(
            title = "3. BATTERY EXEMPTION",
            subtitle = "Prevents background killer from stopping listener",
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
}

@Composable
private fun StepKeyCalibration(
    detectedKeyCode: Int,
    detectedScanCode: Int,
    detectedKeyName: String,
    isKeyRegistered: Boolean,
    onManualSelect: (Int, Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ESSENTIAL KEY SETUP",
                color = NothingRed,
                fontFamily = NdotFont,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "DETECT ESSENTIAL KEY",
                color = TextWhite,
                fontFamily = NdotFont,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Press the physical Essential Key on your phone now. It will be detected automatically.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Animated listening circle
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(if (isKeyRegistered) Color(0xFF1E3A24) else Color(0xFF1A1A1A))
                .border(
                    2.dp,
                    if (isKeyRegistered) Color(0xFF4CAF50) else NothingRed.copy(alpha = 0.8f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isKeyRegistered) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                contentDescription = null,
                tint = if (isKeyRegistered) Color(0xFF4CAF50) else TextWhite,
                modifier = Modifier.size(54.dp)
            )
        }

        // Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, if (isKeyRegistered) Color(0xFF4CAF50) else CardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isKeyRegistered) "READY // CONFIGURED" else "LISTENING FOR KEYPRESS",
                    color = if (isKeyRegistered) Color(0xFF4CAF50) else TextMuted,
                    fontFamily = NdotFont,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = detectedKeyName,
                    color = TextWhite,
                    fontFamily = NdotFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "ScanCode #$detectedScanCode • KeyCode #$detectedKeyCode",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Button(
            onClick = { onManualSelect(0, 250, "ESSENTIAL KEY") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (detectedScanCode == 250) NothingRed else SurfaceDark,
                contentColor = TextWhite
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "ASSIGN ESSENTIAL KEY (SCANCODE 250)", fontSize = 12.sp, fontFamily = NdotFont)
        }
    }
}

@Composable
private fun WizardPermissionCard(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark)
            .border(1.dp, if (isGranted) Color(0xFF2E4E32) else CardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isGranted) Color(0xFF1B2C1E) else Color(0xFF252525)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else TextWhite,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontFamily = NdotFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Text(
            text = if (isGranted) "OK" else "GRANT",
            color = if (isGranted) Color(0xFF4CAF50) else NothingRed,
            fontFamily = NdotFont,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
