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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidianlumina.home.model.AutomationRule
import com.obsidianlumina.home.model.HomeScene
import com.obsidianlumina.home.model.InitialHomeData
import com.obsidianlumina.home.model.SignalTone
import com.obsidianlumina.home.ui.theme.LuminaColors
import com.obsidianlumina.home.viewmodel.HomeUiState

@Composable
fun ScenesScreen(
    uiState: HomeUiState,
    onRunScene: (HomeScene) -> Unit,
    onToggleRule: (String) -> Unit,
    onAddRule: (AutomationRule) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newRuleName by remember { mutableStateOf("") }
    var newRuleTime by remember { mutableStateOf("20:00") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LuminaColors.CanvasBase),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Architectural Scenes",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminaColors.TextPrimary
            )
            Text(
                text = "Synchronize multi-room lighting, climate, and security relays",
                fontSize = 12.sp,
                color = LuminaColors.TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InitialHomeData.scenes.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { scene ->
                            val active = uiState.activeSceneId == scene.id
                            val border = when {
                                !active -> LuminaColors.BorderOff
                                scene.accent == SignalTone.AMBER -> LuminaColors.AmberSignal
                                scene.accent == SignalTone.EMERALD -> LuminaColors.EmeraldSignal
                                else -> LuminaColors.CyanSignal
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = if (active) LuminaColors.TileOn else Color.White,
                                border = BorderStroke(1.dp, border),
                                onClick = { onRunScene(scene) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = scene.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LuminaColors.TextPrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            tint = if (active) LuminaColors.CyanText else LuminaColors.TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = scene.subtitle,
                                        fontSize = 11.sp,
                                        color = LuminaColors.TextSecondary
                                    )
                                    Text(
                                        text = "${scene.actions.size} channels",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = LuminaColors.TextMuted,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Schedules & Rules",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaColors.TextPrimary
                    )
                    Text(
                        text = "Time-based and load-threshold home routines",
                        fontSize = 12.sp,
                        color = LuminaColors.TextMuted
                    )
                }
                OutlinedButton(
                    onClick = { showAddForm = !showAddForm },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("New", fontSize = 12.sp)
                }
            }
        }

        if (showAddForm) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, LuminaColors.BorderOff),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newRuleName,
                            onValueChange = { newRuleName = it },
                            label = { Text("Routine Name") },
                            placeholder = { Text("Evening Porch Illumination") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newRuleTime,
                            onValueChange = { newRuleTime = it },
                            label = { Text("Daily Trigger Time (HH:MM)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (newRuleName.isBlank()) return@Button
                                val scene = InitialHomeData.scenes.first()
                                onAddRule(
                                    AutomationRule(
                                        id = "auto-${System.currentTimeMillis()}",
                                        name = newRuleName.trim(),
                                        triggerType = "time",
                                        triggerValue = "Daily · $newRuleTime",
                                        targetSummary = "${scene.name} Scene (${scene.actions.size} relays)",
                                        enabled = true,
                                        sceneId = scene.id
                                    )
                                )
                                newRuleName = ""
                                showAddForm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminaColors.TextPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Routine", color = Color.White)
                        }
                    }
                }
            }
        }

        items(uiState.automations, key = { it.id }) { rule ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, LuminaColors.BorderOff),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = LuminaColors.CyanText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = rule.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LuminaColors.TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${rule.triggerValue} · ${rule.targetSummary}",
                            fontSize = 12.sp,
                            color = LuminaColors.TextMuted
                        )
                        if (rule.sceneId != null) {
                            TextButton(
                                onClick = {
                                    InitialHomeData.scenes.find { it.id == rule.sceneId }?.let(onRunScene)
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Run now", fontSize = 12.sp, color = LuminaColors.CyanText)
                            }
                        }
                    }

                    Switch(
                        checked = rule.enabled,
                        onCheckedChange = { onToggleRule(rule.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = LuminaColors.CyanText
                        )
                    )
                }
            }
        }
    }
}
