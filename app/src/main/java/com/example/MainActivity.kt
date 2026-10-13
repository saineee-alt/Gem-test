package com.example

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.AppSettingsRepository
import com.example.data.CapsuleSettings
import com.example.overlay.CapsuleOverlayService
import com.example.ui.settings.AdvancedSettingsScreen
import com.example.ui.settings.IsletHomeScreen
import com.example.ui.settings.PermissionOnboardingScreen
import com.example.ui.settings.StyleScreen
import com.example.ui.settings.TestStudioScreen
import com.example.ui.theme.IsletTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: AppSettingsRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = AppSettingsRepository(applicationContext)

        setContent {
            IsletTheme {
                val settings by repository.settingsFlow.collectAsState(initial = CapsuleSettings())
                val snackbarHostState = remember { SnackbarHostState() }

                var selectedTab by remember { mutableIntStateOf(0) }
                var showPermissionsFlow by remember { mutableStateOf(false) }
                var showGuideDialog by remember { mutableStateOf(false) }

                // Automatically launch overlay service if permitted
                LaunchedEffect(settings.overlayEnabled) {
                    if (settings.overlayEnabled && Settings.canDrawOverlays(this@MainActivity)) {
                        CapsuleOverlayService.start(this@MainActivity)
                    }
                }

                if (showPermissionsFlow) {
                    PermissionOnboardingScreen(
                        onDismiss = {
                            showPermissionsFlow = false
                            if (Settings.canDrawOverlays(this@MainActivity) && settings.overlayEnabled) {
                                CapsuleOverlayService.start(this@MainActivity)
                            }
                        }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = when (selectedTab) {
                                            0 -> "Islet"
                                            1 -> "Style & Cutout"
                                            2 -> "Settings"
                                            3 -> "Test Studio"
                                            else -> "Islet"
                                        },
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = { showPermissionsFlow = true },
                                        modifier = Modifier.testTag("permissions_nav_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "Permissions",
                                            tint = if (Settings.canDrawOverlays(this@MainActivity))
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.error
                                        )
                                    }
                                    TextButton(onClick = { showGuideDialog = true }) {
                                        Text("Guide", style = MaterialTheme.typography.labelLarge)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.Adjust, contentDescription = "Capsule") },
                                    label = { Text("Capsule") },
                                    modifier = Modifier.testTag("nav_tab_capsule")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.Tune, contentDescription = "Style") },
                                    label = { Text("Style") },
                                    modifier = Modifier.testTag("nav_tab_style")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    modifier = Modifier.testTag("nav_tab_settings")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    icon = { Icon(Icons.Default.Science, contentDescription = "Test Lab") },
                                    label = { Text("Test Lab") },
                                    modifier = Modifier.testTag("nav_tab_test_lab")
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "tab_transition"
                            ) { tab ->
                                when (tab) {
                                    0 -> IsletHomeScreen(
                                        settings = settings,
                                        repository = repository,
                                        onOpenPermissions = { showPermissionsFlow = true }
                                    )
                                    1 -> StyleScreen(
                                        settings = settings,
                                        repository = repository
                                    )
                                    2 -> AdvancedSettingsScreen(
                                        settings = settings,
                                        repository = repository,
                                        snackbarHostState = snackbarHostState
                                    )
                                    3 -> TestStudioScreen()
                                }
                            }
                        }
                    }
                }

                if (showGuideDialog) {
                    GuideDialog(onDismiss = { showGuideDialog = false })
                }
            }
        }
    }
}

@Composable
private fun GuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Welcome to Islet") },
        text = {
            Text(
                text = "Islet renders a Material You dynamic pill capsule anchored to your device's front camera cutout.\n\n" +
                        "• Tap the capsule to expand it into rich cards with battery stats, music controls, and notifications.\n" +
                        "• Swipe down to expand, swipe up to dismiss.\n" +
                        "• Calibrate offset and width in the 'Style' tab to align with your exact hardware cutout.\n" +
                        "• Use the 'Test Lab' tab to try out cards immediately without plugging in cables."
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Got it") }
        }
    )
}
