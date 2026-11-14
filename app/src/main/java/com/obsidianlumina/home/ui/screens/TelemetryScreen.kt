package com.obsidianlumina.home.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidianlumina.home.model.HomeDevice
import com.obsidianlumina.home.model.RoomId
import com.obsidianlumina.home.ui.theme.LuminaColors
import com.obsidianlumina.home.viewmodel.HomeUiState
import java.util.Locale

@Composable
fun TelemetryScreen(
    uiState: HomeUiState,
    onSelectDevice: (HomeDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalWatts = uiState.devices.sumOf { it.currentWatts() }
    val amps = totalWatts / uiState.bridge.gridVoltage.toDouble()
    val activeCount = uiState.devices.count { it.isOn }
    val avgLatency = if (uiState.devices.isNotEmpty()) {
        uiState.devices.sumOf { it.latencyMs } / uiState.devices.size
    } else 0
    val estDailyKwh = (totalWatts * 8.5) / 1000.0
    val estDailyCost = estDailyKwh * uiState.bridge.ratePerKwh

    val roomLoads = RoomId.entries
        .filter { it != RoomId.ALL }
        .map { room ->
            val list = uiState.devices.filter { it.roomId == room }
            Triple(room, list.sumOf { it.currentWatts() }, list.count { it.isOn } to list.size)
        }
        .sortedByDescending { it.second }
    val maxRoomWatts = (roomLoads.maxOfOrNull { it.second } ?: 100.0).coerceAtLeast(100.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuminaColors.CanvasBase),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricBox(
                    title = "Instantaneous Load",
                    value = if (totalWatts >= 1000.0) {
                        String.format(Locale.US, "%.2f kW", totalWatts / 1000.0)
                    } else {
                        String.format(Locale.US, "%.1f W", totalWatts)
                    },
                    sub = "$activeCount / ${uiState.devices.size} energized",
                    subColor = LuminaColors.CyanText,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    title = "Bus Voltage & Amps",
                    value = String.format(Locale.US, "%dV · %.1fA", uiState.bridge.gridVoltage, amps),
                    sub = "50/60 Hz AC",
                    subColor = LuminaColors.TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricBox(
                    title = "Projected Daily Draw",
                    value = String.format(Locale.US, "%.1f kWh", estDailyKwh),
                    sub = String.format(Locale.US, "~%s%.2f / day", uiState.bridge.currency, estDailyCost),
                    subColor = LuminaColors.AmberText,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    title = "Mean Latency",
                    value = "$avgLatency ms",
                    sub = uiState.bridge.mode.label,
                    subColor = LuminaColors.EmeraldText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, LuminaColors.BorderOff),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Load by Room Zone",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaColors.TextPrimary
                    )
                    roomLoads.forEach { (room, watts, counts) ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${room.displayName} · ${counts.first}/${counts.second}",
                                    fontSize = 12.sp,
                                    color = LuminaColors.TextPrimary
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f W", watts),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LuminaColors.TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(LuminaColors.RecessedWell)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((watts / maxRoomWatts).toFloat().coerceIn(0.04f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(LuminaColors.CyanSignal, LuminaColors.AmberSignal)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.events.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, LuminaColors.BorderOff),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Relay Dispatch Log",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LuminaColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.events.take(5).forEachIndexed { idx, ev ->
                            if (idx > 0) HorizontalDivider(color = LuminaColors.Hairline)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(ev.deviceName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${ev.timestamp} · ${ev.transport}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = LuminaColors.TextMuted
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        ev.action,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = if (ev.ok) LuminaColors.CyanText else LuminaColors.EmergencyRed
                                    )
                                    Text(
                                        "${ev.latencyMs} ms",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = LuminaColors.TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Hardware Node Registry",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminaColors.TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(uiState.devices, key = { it.id }) { device ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, LuminaColors.BorderOff),
                onClick = { onSelectDevice(device) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (device.isOn) LuminaColors.CyanSignal else LuminaColors.BorderOff)
                        )
                        Column {
                            Text(
                                text = device.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = LuminaColors.TextPrimary
                            )
                            Text(
                                text = "${device.ip} · ${device.mac} · ${device.channel}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = LuminaColors.TextMuted
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "%.1f W", device.currentWatts()),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuminaColors.TextPrimary
                        )
                        Text(
                            text = "${device.latencyMs} ms",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = LuminaColors.EmeraldText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    sub: String,
    subColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LuminaColors.BorderOff)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = LuminaColors.TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminaColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sub,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = subColor
            )
        }
    }
}
