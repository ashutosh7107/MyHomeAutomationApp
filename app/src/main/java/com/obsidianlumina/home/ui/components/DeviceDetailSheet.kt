package com.obsidianlumina.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.obsidianlumina.home.ui.theme.LuminaColors
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailSheet(
    device: HomeDevice,
    roomName: String,
    onDismiss: () -> Unit,
    onUpdate: (HomeDevice, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBindings by remember(device.id) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = device.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminaColors.TextPrimary
            )
            Text(
                text = "$roomName · ${device.channel} · ${device.ip}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = LuminaColors.TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Master Relay Power Row
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LuminaColors.RecessedWell,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (device.type == DeviceType.LOCK) "Lock State" else "Relay Power",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuminaColors.TextPrimary
                        )
                        Text(
                            text = if (device.isOn) {
                                String.format(Locale.US, "Active · %.1f W", device.currentWatts())
                            } else {
                                String.format(Locale.US, "Standby · %.1f W", device.standbyWatts)
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = LuminaColors.TextMuted
                        )
                    }

                    Button(
                        onClick = { onUpdate(device.copy(isOn = !device.isOn), true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (device.isOn) LuminaColors.CyanSignal.copy(alpha = 0.2f) else Color.White,
                            contentColor = if (device.isOn) LuminaColors.CyanDarkText else LuminaColors.TextSecondary
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (device.isOn) LuminaColors.CyanSignal else LuminaColors.BorderOff
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (device.type == DeviceType.LOCK) {
                                if (device.isOn) "Unlock" else "Lock"
                            } else {
                                if (device.isOn) "Turn Off" else "Turn On"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Dimmer or Blind Slider
            if (device.type == DeviceType.DIMMER || device.type == DeviceType.BLIND) {
                Spacer(modifier = Modifier.height(18.dp))
                val value = if (device.type == DeviceType.DIMMER) device.brightness else device.position
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (device.type == DeviceType.DIMMER) "Luminous Intensity" else "Blind Position",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = LuminaColors.TextPrimary
                    )
                    Text(
                        text = "$value%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaColors.CyanText
                    )
                }
                Slider(
                    value = value.toFloat(),
                    onValueChange = { raw ->
                        val next = raw.roundToInt()
                        val patched = if (device.type == DeviceType.DIMMER) {
                            device.copy(brightness = next, isOn = next > 0)
                        } else {
                            device.copy(position = next, isOn = next > 0)
                        }
                        onUpdate(patched, false)
                    },
                    onValueChangeFinished = {
                        onUpdate(device, true)
                    },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = LuminaColors.CyanText,
                        activeTrackColor = LuminaColors.CyanSignal,
                        inactiveTrackColor = LuminaColors.RecessedWell
                    )
                )

                if (device.type == DeviceType.DIMMER) {
                    Text(
                        text = "Color Temperature",
                        fontSize = 12.sp,
                        color = LuminaColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LuminaColors.RecessedWell)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(2700 to "2700K Warm", 3500 to "3500K Soft", 5200 to "5200K Day").forEach { (k, label) ->
                            val selected = device.kelvin == k
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = if (selected) Color.White else Color.Transparent,
                                shadowElevation = if (selected) 2.dp else 0.dp,
                                onClick = { onUpdate(device.copy(kelvin = k, isOn = true), true) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) LuminaColors.TextPrimary else LuminaColors.TextMuted,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fan Speed Selector
            if (device.type == DeviceType.FAN) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Motor Speed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LuminaColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LuminaColors.RecessedWell)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3).forEach { spd ->
                        val active = device.isOn && device.fanSpeed == spd
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Color.White else Color.Transparent,
                            shadowElevation = if (active) 2.dp else 0.dp,
                            onClick = { onUpdate(device.copy(fanSpeed = spd, isOn = true), true) }
                        ) {
                            Text(
                                text = "Speed $spd",
                                fontSize = 13.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (active) LuminaColors.CyanText else LuminaColors.TextMuted,
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp)
                            )
                        }
                    }
                }
            }

            // Climate Thermostat Stepper
            if (device.type == DeviceType.CLIMATE) {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LuminaColors.RecessedWell,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Target Temperature",
                                fontSize = 12.sp,
                                color = LuminaColors.TextMuted
                            )
                            Text(
                                text = "${device.targetTemp}°C",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = LuminaColors.TextPrimary
                            )
                            Text(
                                text = "Room Sensor: ${device.currentTemp}°C",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = LuminaColors.TextMuted
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onUpdate(
                                        device.copy(
                                            targetTemp = (device.targetTemp - 1).coerceAtLeast(16),
                                            isOn = true
                                        ),
                                        true
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                androidx.compose.material3.Icon(Icons.Rounded.Remove, contentDescription = "Cooler")
                            }
                            OutlinedButton(
                                onClick = {
                                    onUpdate(
                                        device.copy(
                                            targetTemp = (device.targetTemp + 1).coerceAtMost(30),
                                            isOn = true
                                        ),
                                        true
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                androidx.compose.material3.Icon(Icons.Rounded.Add, contentDescription = "Warmer")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = LuminaColors.Hairline)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hardware Node & Bridge Binding",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = LuminaColors.TextPrimary
                )
                TextButton(onClick = { showBindings = !showBindings }) {
                    Text(
                        text = if (showBindings) "Hide" else "Configure",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = LuminaColors.CyanText
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("MAC Address", fontSize = 12.sp, color = LuminaColors.TextMuted)
                Text(device.mac, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = LuminaColors.TextPrimary)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Latency", fontSize = 12.sp, color = LuminaColors.TextMuted)
                Text("${device.latencyMs} ms", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = LuminaColors.EmeraldText)
            }

            if (showBindings) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = device.haEntityId,
                    onValueChange = { onUpdate(device.copy(haEntityId = it), false) },
                    label = { Text("Home Assistant Entity ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = device.webhookOnUrl,
                    onValueChange = { onUpdate(device.copy(webhookOnUrl = it), false) },
                    label = { Text("LAN Webhook ON URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = device.webhookOffUrl,
                    onValueChange = { onUpdate(device.copy(webhookOffUrl = it), false) },
                    label = { Text("LAN Webhook OFF URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        onDelete(device.id)
                        onDismiss()
                    },
                    border = BorderStroke(1.dp, LuminaColors.EmergencyBorder.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = LuminaColors.EmergencyRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Remove Switch Plate", color = LuminaColors.EmergencyRed, fontSize = 13.sp)
                }
            }
        }
    }
}
