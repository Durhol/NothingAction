package com.nothing.action

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nothing.action.core.data.ActionPreferencesRepository
import com.nothing.action.service.NothingAccessibilityService
import com.nothing.action.ui.about.AboutScreen
import com.nothing.action.ui.home.HomeScreen
import com.nothing.action.ui.home.HomeViewModel
import com.nothing.action.ui.onboarding.OnboardingScreen
import com.nothing.action.ui.settings.SettingsScreen
import com.nothing.action.ui.theme.NothingActionTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val repository by lazy { ActionPreferencesRepository(this) }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Result handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            NothingActionTheme {
                val navController = rememberNavController()
                val settings by repository.settingsFlow.collectAsState(initial = null)

                if (settings != null) {
                    val startDestination = if (settings!!.isOnboardingCompleted) "home" else "onboarding"

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinished = {
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToAbout = { navController.navigate("about") }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onRerunOnboarding = { navController.navigate("onboarding") }
                            )
                        }

                        composable("about") {
                            AboutScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (NothingAccessibilityService.isCalibratingKey && event != null) {
            val scanCode = event.scanCode
            val keyName = when {
                scanCode == 250 -> "ESSENTIAL KEY"
                keyCode == KeyEvent.KEYCODE_ASSIST -> "ASSIST KEY"
                scanCode > 0 -> "KEY (SCANCODE $scanCode)"
                keyCode != KeyEvent.KEYCODE_UNKNOWN -> KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
                else -> "KEY #$scanCode"
            }
            val broadcastIntent = Intent(NothingAccessibilityService.ACTION_KEY_CALIBRATION_EVENT).apply {
                setPackage(packageName)
                putExtra(NothingAccessibilityService.EXTRA_KEY_CODE, keyCode)
                putExtra(NothingAccessibilityService.EXTRA_KEY_SCAN_CODE, scanCode)
                putExtra(NothingAccessibilityService.EXTRA_KEY_NAME, keyName)
            }
            sendBroadcast(broadcastIntent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
