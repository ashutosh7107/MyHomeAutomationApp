package com.obsidianlumina.home.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidianlumina.home.model.BridgeConfig
import com.obsidianlumina.home.model.BridgeMode
import com.obsidianlumina.home.ui.theme.LuminaColors
import com.obsidianlumina.home.viewmodel.HomeUiState

@Composable
fun SettingsScreen(
    uiState: HomeUiState,
    onUpdateBridge: (BridgeConfig) -> Unit,
    onTestHomeAssistant: () -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bridge = uiState.bridge

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuminaColors.CanvasBase),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, LuminaColors.BorderOff),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Hardware Control Bridge",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaColors.TextPrimary
                    )
                    Text(
                        text = "Connect directly to Home Assistant or local Tasmota / Shelly / ESPHome relays over Wi-Fi.",
                        fontSize = 12.sp,
                        color = LuminaColors.TextMuted
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LuminaColors.RecessedWell)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BridgeMode.entries.forEach { mode ->
                            val selected = bridge.mode == mode
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) Color.White else Color.Transparent,
                                shadowElevation = if (selected) 2.dp else 0.dp,
                                onClick = { onUpdateBridge(bridge.copy(mode = mode)) }
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) LuminaColors.CyanText else LuminaColors.TextMuted,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                )
                            }
                        }
                    }

                    if (bridge.mode == BridgeMode.HOME_ASSISTANT) {
                        OutlinedTextField(
                            value = bridge.haUrl,
                            onValueChange = { onUpdateBridge(bridge.copy(haUrl = it)) },
                            label = { Text("Home Assistant Server URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bridge.haToken,
                            onValueChange = { onUpdateBridge(bridge.copy(haToken = it)) },
                            label = { Text("Long-Lived Access Token") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = onTestHomeAssistant,
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaColors.TextPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Test Connection", color = Color.White, fontSize = 13.sp)
                        }
                        if (uiState.haTestStatus != null) {
                            Text(
                                text = uiState.haTestStatus,
                                fontSize = 12.sp,
                                color = LuminaColors.CyanText
                            )
                        }
                    }

                    if (bridge.mode == BridgeMode.WEBHOOK) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LuminaColors.RecessedWell,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tap the Tune icon on any switch plate in Control to assign its local LAN HTTP ON/OFF endpoint.",
                                fontSize = 12.sp,
                                color = LuminaColors.TextSecondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, LuminaColors.BorderOff),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Home Profile & Grid",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaColors.TextPrimary
                    )

                    OutlinedTextField(
                        value = bridge.homeName,
                        onValueChange = { onUpdateBridge(bridge.copy(homeName = it)) },
                        label = { Text("Residence Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = bridge.currency,
                            onValueChange = { onUpdateBridge(bridge.copy(currency = it.take(3))) },
                            label = { Text("Currency") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bridge.ratePerKwh.toString(),
                            onValueChange = {
                                val parsed = it.toDoubleOrNull()
                                if (parsed != null) onUpdateBridge(bridge.copy(ratePerKwh = parsed))
                            },
                            label = { Text("Rate / kWh") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(230, 120).forEach { volts ->
                            val selected = bridge.gridVoltage == volts
                            OutlinedButton(
                                onClick = { onUpdateBridge(bridge.copy(gridVoltage = volts)) },
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) LuminaColors.CyanSignal else LuminaColors.BorderOff
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${volts}V Grid",
                                    color = if (selected) LuminaColors.CyanText else LuminaColors.TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = onResetDefaults,
                        border = BorderStroke(1.dp, LuminaColors.EmergencyBorder.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Home Defaults", color = LuminaColors.EmergencyRed, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
