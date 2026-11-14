package com.obsidianlumina.home.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidianlumina.home.model.DeviceType
import com.obsidianlumina.home.model.HomeDevice
import com.obsidianlumina.home.model.HomeScene
import com.obsidianlumina.home.model.InitialHomeData
import com.obsidianlumina.home.model.RoomId
import com.obsidianlumina.home.model.SignalTone
import com.obsidianlumina.home.ui.components.TouchTile
import com.obsidianlumina.home.ui.theme.LuminaColors
import com.obsidianlumina.home.viewmodel.HomeUiState
import java.util.Locale

@Composable
fun ControlScreen(
    uiState: HomeUiState,
    onSelectRoom: (RoomId) -> Unit,
    onToggleDevice: (String) -> Unit,
    onOpenDetail: (HomeDevice) -> Unit,
    onRunScene: (HomeScene) -> Unit,
    onMasterCutoff: () -> Unit,
    modifier: Modifier = Modifier
) {
    val devices = if (uiState.selectedRoom == RoomId.ALL) {
        uiState.devices
    } else {
        uiState.devices.filter { it.roomId == uiState.selectedRoom }
    }

    val totalWatts = uiState.devices.sumOf { it.currentWatts() }
    val activeRelays = uiState.devices.count { it.isOn && it.type != DeviceType.LOCK }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LuminaColors.CanvasBase)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Zone Headline & Master Cutoff
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.selectedRoom.displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuminaColors.TextPrimary
                )
                val powerLabel = if (totalWatts >= 1000.0) {
                    String.format(Locale.US, "%.2f kW", totalWatts / 1000.0)
                } else {
                    String.format(Locale.US, "%.0f W", totalWatts)
                }
                Text(
                    text = "$activeRelays active relays · $powerLabel · ${uiState.bridge.mode.label}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = LuminaColors.TextMuted
                )
            }

            OutlinedButton(
                onClick = onMasterCutoff,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LuminaColors.EmergencyBorder.copy(alpha = 0.55f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerOff,
                    contentDescription = "Master Cutoff",
                    tint = LuminaColors.EmergencyRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Cutoff",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = LuminaColors.EmergencyRed
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Room Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LuminaColors.RecessedWell)
                .horizontalScroll(rememberScrollState())
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RoomId.entries.forEach { room ->
                val selected = uiState.selectedRoom == room
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    shadowElevation = if (selected) 2.dp else 0.dp,
                    onClick = { onSelectRoom(room) }
                ) {
                    Text(
                        text = room.shortName,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) LuminaColors.CyanText else LuminaColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Scene Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InitialHomeData.scenes.forEach { scene ->
                val active = uiState.activeSceneId == scene.id
                val border = when {
                    !active -> LuminaColors.BorderOff
                    scene.accent == SignalTone.AMBER -> LuminaColors.AmberSignal
                    scene.accent == SignalTone.EMERALD -> LuminaColors.EmeraldSignal
                    else -> LuminaColors.CyanSignal
                }
                val textColor = when {
                    !active -> LuminaColors.TextSecondary
                    scene.accent == SignalTone.AMBER -> LuminaColors.AmberText
                    scene.accent == SignalTone.EMERALD -> LuminaColors.EmeraldText
                    else -> LuminaColors.CyanDarkText
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (active) LuminaColors.TileOn else Color.White,
                    border = BorderStroke(1.dp, border),
                    onClick = { onRunScene(scene) }
                ) {
                    Text(
                        text = scene.name,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2-Column Switch Plate Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(devices, key = { it.id }) { device ->
                TouchTile(
                    device = device,
                    roomName = device.roomId.shortName,
                    onToggle = { onToggleDevice(device.id) },
                    onOpenDetail = { onOpenDetail(device) }
                )
            }
        }
    }
}
