package com.animdex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.animdex.app.ui.AnimalResultBottomSheet
import com.animdex.app.ui.AnimDexViewModel
import com.animdex.app.ui.CameraScannerScreen
import com.animdex.app.ui.DexCollectionScreen
import com.animdex.app.ui.SettingsScreen
import com.animdex.app.ui.theme.AnimDexTheme

enum class AppTab(val label: String) {
    SCANNER("Scanner"),
    COLLECTION("AnimDex"),
    SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: AnimDexViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentThemeMode by viewModel.themeMode.collectAsState()
            val currentAccent by viewModel.accentColor.collectAsState()

            AnimDexTheme(
                themeMode = currentThemeMode,
                accent = currentAccent
            ) {
                var currentTab by remember { mutableStateOf(AppTab.SCANNER) }
                val scanState by viewModel.scanState.collectAsState()
                val entries by viewModel.entries.collectAsState()
                val totalCatches by viewModel.totalCatches.collectAsState()

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.SCANNER,
                                onClick = { currentTab = AppTab.SCANNER },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Scanner"
                                    )
                                },
                                label = { Text("Scanner") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.COLLECTION,
                                onClick = { currentTab = AppTab.COLLECTION },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CollectionsBookmark,
                                        contentDescription = "AnimDex"
                                    )
                                },
                                label = { Text("AnimDex") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.SETTINGS,
                                onClick = { currentTab = AppTab.SETTINGS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.SCANNER -> {
                                CameraScannerScreen(
                                    selectedMode = scanState.selectedMode,
                                    onModeSelected = { mode -> viewModel.setModelMode(mode) },
                                    onImageCaptured = { bitmap -> viewModel.onImageCaptured(bitmap) },
                                    isAnalyzing = scanState.isAnalyzing
                                )
                            }
                            AppTab.COLLECTION -> {
                                DexCollectionScreen(
                                    entries = entries,
                                    totalCount = totalCatches,
                                    onDeleteEntry = { entry -> viewModel.deleteEntry(entry) }
                                )
                            }
                            AppTab.SETTINGS -> {
                                SettingsScreen(
                                    currentThemeMode = currentThemeMode,
                                    currentAccent = currentAccent,
                                    totalDiscoveries = totalCatches,
                                    onThemeModeChanged = { mode -> viewModel.setThemeMode(mode) },
                                    onAccentColorChanged = { color -> viewModel.setAccentColor(color) }
                                )
                            }
                        }

                        // Bottom Sheet Popup when image is analyzed
                        if (scanState.showResultSheet) {
                            AnimalResultBottomSheet(
                                sheetState = sheetState,
                                bitmap = scanState.scannedBitmap,
                                results = scanState.results,
                                isRealWildlife = scanState.isRealWildlife,
                                spoofReason = scanState.spoofReason,
                                onDismiss = { viewModel.dismissResultSheet() },
                                onSaveToDex = { result ->
                                    viewModel.saveResultToDex(result)
                                    currentTab = AppTab.COLLECTION
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
