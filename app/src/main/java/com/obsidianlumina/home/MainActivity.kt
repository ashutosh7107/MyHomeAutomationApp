package com.obsidianlumina.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.obsidianlumina.home.ui.components.AddDeviceDialog
import com.obsidianlumina.home.ui.components.DeviceDetailSheet
import com.obsidianlumina.home.ui.screens.ControlScreen
import com.obsidianlumina.home.ui.screens.ScenesScreen
import com.obsidianlumina.home.ui.screens.SettingsScreen
import com.obsidianlumina.home.ui.screens.TelemetryScreen
import com.obsidianlumina.home.ui.theme.LuminaColors
import com.obsidianlumina.home.ui.theme.ObsidianLuminaTheme
import com.obsidianlumina.home.viewmodel.HomeViewModel

enum class AppTab(val label: String, val icon: ImageVector) {
    CONTROL("Control", Icons.Rounded.GridView),
    SCENES("Scenes", Icons.Rounded.PlayCircle),
    TELEMETRY("Telemetry", Icons.Rounded.Analytics),
    HUB("Hub", Icons.Rounded.Tune)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObsidianLuminaTheme {
                ObsidianLuminaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObsidianLuminaApp(vm: HomeViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.CONTROL) }
    var showAddDevice by remember { mutableStateOf(false) }

    val detailDevice = uiState.devices.find { it.id == uiState.detailDeviceId }

    Scaffold(
        containerColor = LuminaColors.CanvasBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.bridge.homeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaColors.TextPrimary
                    )
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddDevice = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = LuminaColors.TextPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuminaColors.CanvasBase
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuminaColors.CyanText,
                            selectedTextColor = LuminaColors.CyanText,
                            indicatorColor = LuminaColors.CyanSignal.copy(alpha = 0.16f),
                            unselectedIconColor = LuminaColors.TextMuted,
                            unselectedTextColor = LuminaColors.TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.CONTROL -> ControlScreen(
                uiState = uiState,
                onSelectRoom = vm::selectRoom,
                onToggleDevice = vm::toggleDevice,
                onOpenDetail = { vm.openDetail(it.id) },
                onRunScene = vm::runScene,
                onMasterCutoff = vm::masterCutoff,
                modifier = Modifier.padding(innerPadding)
            )

            AppTab.SCENES -> ScenesScreen(
                uiState = uiState,
                onRunScene = vm::runScene,
                onToggleRule = vm::toggleRule,
                onAddRule = vm::addRule,
                modifier = Modifier.padding(innerPadding)
            )

            AppTab.TELEMETRY -> TelemetryScreen(
                uiState = uiState,
                onSelectDevice = { vm.openDetail(it.id) },
                modifier = Modifier.padding(innerPadding)
            )

            AppTab.HUB -> SettingsScreen(
                uiState = uiState,
                onUpdateBridge = vm::updateBridge,
                onTestHomeAssistant = vm::testHomeAssistant,
                onResetDefaults = vm::resetDefaults,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (detailDevice != null) {
        DeviceDetailSheet(
            device = detailDevice,
            roomName = detailDevice.roomId.displayName,
            onDismiss = { vm.openDetail(null) },
            onUpdate = vm::updateDevice,
            onDelete = vm::deleteDevice
        )
    }

    if (showAddDevice) {
        AddDeviceDialog(
            onDismiss = { showAddDevice = false },
            onAdd = vm::addDevice
        )
    }
}
