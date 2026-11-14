package com.obsidianlumina.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidianlumina.home.model.DeviceType
import com.obsidianlumina.home.model.HomeDevice
import com.obsidianlumina.home.model.RoomId
import com.obsidianlumina.home.model.SignalTone
import com.obsidianlumina.home.ui.theme.LuminaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAdd: (HomeDevice) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf(RoomId.LIVING) }
    var selectedType by remember { mutableStateOf(DeviceType.LIGHT) }
    var selectedTone by remember { mutableStateOf(SignalTone.CYAN) }
    var wattsText by remember { mutableStateOf("24") }
    var ipText by remember { mutableStateOf("192.168.1.90") }
    var haEntity by remember { mutableStateOf("") }

    var roomExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Switch or Appliance",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminaColors.TextPrimary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Switch Name") },
                placeholder = { Text("Dining Chandelier") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = roomExpanded,
                    onExpandedChange = { roomExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedRoom.shortName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roomExpanded,
                        onDismissRequest = { roomExpanded = false }
                    ) {
                        RoomId.entries.filter { it != RoomId.ALL }.forEach { room ->
                            DropdownMenuItem(
                                text = { Text(room.displayName) },
                                onClick = {
                                    selectedRoom = room
                                    roomExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        DeviceType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedType = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = wattsText,
                    onValueChange = { wattsText = it },
                    label = { Text("Load (W)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ipText,
                    onValueChange = { ipText = it },
                    label = { Text("Local IP") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = haEntity,
                onValueChange = { haEntity = it },
                label = { Text("HA Entity ID (Optional)") },
                placeholder = { Text("light.dining_chandelier") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val device = HomeDevice(
                        id = "dev-${System.currentTimeMillis()}",
                        name = name.trim(),
                        roomId = selectedRoom,
                        type = selectedType,
                        isOn = false,
                        signalTone = selectedTone,
                        watts = wattsText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 18.0,
                        ip = ipText.ifBlank { "192.168.1.90" },
                        channel = "CH-${(12..98).random()}",
                        haEntityId = haEntity.trim()
                    )
                    onAdd(device)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuminaColors.TextPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Add Switch Plate", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}
