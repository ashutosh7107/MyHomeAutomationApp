package com.obsidianlumina.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WindPower
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidianlumina.home.data.IrBlaster
import com.obsidianlumina.home.data.SwitchRepository
import com.obsidianlumina.home.model.HomeSwitch
import com.obsidianlumina.home.model.SwitchType
import com.obsidianlumina.home.model.SwitchboardState
import com.obsidianlumina.home.ui.theme.LuminaColors
import com.obsidianlumina.home.ui.theme.ObsidianLuminaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObsidianLuminaTheme {
                LivingRoomSwitchboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivingRoomSwitchboardScreen() {
    val context = LocalContext.current
    val repo = remember { SwitchRepository(context) }
    val irBlaster = remember { IrBlaster(context) }
    val hasIr = remember { irBlaster.hasEmitter() }
    val scope = rememberCoroutineScope()

    var board by remember { mutableStateOf(repo.load()) }
    var editMode by remember { mutableStateOf(false) }
    var editingSwitch by remember { mutableStateOf<HomeSwitch?>(null) }
    var editingStepUrls by remember { mutableStateOf(false) }

    fun saveBoard(next: SwitchboardState) {
        board = next
        repo.save(next)
    }

    fun getSwitch(num: Int): HomeSwitch =
        board.switches.first { it.number == num }

    fun toggleSwitch(num: Int) {
        val sw = getSwitch(num)
        if (editMode) {
            editingSwitch = sw
            return
        }
        val nextOn = !sw.isOn
        saveBoard(
            board.copy(
                switches = board.switches.map {
                    if (it.number == num) it.copy(isOn = nextOn) else it
                }
            )
        )
        if (sw.irCode.isNotBlank()) {
            irBlaster.transmit(sw.irCode)
        }
        val url = if (nextOn) sw.onUrl else sw.offUrl
        if (url.isNotBlank()) scope.launch { repo.triggerUrl(url) }
    }

    fun setAll(on: Boolean) {
        if (editMode) {
            editingStepUrls = true
            return
        }
        val targets = board.switches.filter { it.isOn != on }
        saveBoard(board.copy(switches = board.switches.map { it.copy(isOn = on) }))
        val masterIr = if (on) board.allOnIrCode else board.allOffIrCode
        if (masterIr.isNotBlank()) {
            irBlaster.transmit(masterIr)
        }
        scope.launch {
            targets.forEach { sw ->
                val url = if (on) sw.onUrl else sw.offUrl
                if (url.isNotBlank()) repo.triggerUrl(url)
            }
        }
    }

    fun stepLevel(delta: Int) {
        if (editMode) {
            editingStepUrls = true
            return
        }
        val nextLevel = (board.level + delta).coerceIn(1, 5)
        saveBoard(board.copy(level = nextLevel))
        val stepIr = if (delta > 0) board.plusIrCode else board.minusIrCode
        if (stepIr.isNotBlank()) {
            irBlaster.transmit(stepIr)
        }
        val url = if (delta > 0) board.plusUrl else board.minusUrl
        if (url.isNotBlank()) scope.launch { repo.triggerUrl(url) }
    }

    val activeCount = board.switches.count { it.isOn }

    Scaffold(
        containerColor = LuminaColors.CanvasBase,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "B-502 Automation",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LuminaColors.TextPrimary
                        )
                        Text(
                            text = "Living Room · $activeCount / 8 ON · Level ${board.level}/5",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = LuminaColors.TextMuted
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { editMode = !editMode },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (editMode) LuminaColors.CyanSignal else LuminaColors.BorderOff
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (editMode) LuminaColors.CyanSignal.copy(alpha = 0.14f) else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (editMode) Icons.Rounded.Done else Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = if (editMode) LuminaColors.CyanText else LuminaColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(
                            text = if (editMode) "Done" else "Edit Labels",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (editMode) LuminaColors.CyanText else LuminaColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LuminaColors.CanvasBase)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (editMode) {
                Text(
                    text = if (hasIr) {
                        "IR Blaster active · Tap #1–#8, +/-, or All On/Off to edit labels & IR/Wi-Fi"
                    } else {
                        "Tap #1–#8, +/-, or All On/Off to edit labels, IR hex, or Wi-Fi URLs"
                    },
                    fontSize = 12.sp,
                    color = LuminaColors.CyanText,
                    modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                )
            }

            // Glass Switchboard Housing: Exact 2-Row x 6-Column Layout
            // Row 1: [ + ] | [ #1 ] | [ #2 ] | [ All On ] | [ #5 ] | [ #6 ]
            // Row 2: [ - ] | [ #3 ] | [ #4 ] | [ All Off] | [ #7 ] | [ #8 ]
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = LuminaColors.RecessedWell,
                border = BorderStroke(1.dp, LuminaColors.BorderOff),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ROW 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionPlateKey(
                            label = "Lvl ${board.level}",
                            icon = Icons.Rounded.Add,
                            accentColor = LuminaColors.CyanText,
                            borderColor = LuminaColors.BorderOff,
                            onClick = { stepLevel(+1) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(1),
                            editMode = editMode,
                            onClick = { toggleSwitch(1) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(2),
                            editMode = editMode,
                            onClick = { toggleSwitch(2) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionPlateKey(
                            label = "All On",
                            icon = Icons.Rounded.PowerSettingsNew,
                            accentColor = LuminaColors.EmeraldText,
                            borderColor = LuminaColors.EmeraldSignal.copy(alpha = 0.6f),
                            onClick = { setAll(true) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(5),
                            editMode = editMode,
                            onClick = { toggleSwitch(5) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(6),
                            editMode = editMode,
                            onClick = { toggleSwitch(6) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ROW 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionPlateKey(
                            label = "Dim -",
                            icon = Icons.Rounded.Remove,
                            accentColor = LuminaColors.TextSecondary,
                            borderColor = LuminaColors.BorderOff,
                            onClick = { stepLevel(-1) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(3),
                            editMode = editMode,
                            onClick = { toggleSwitch(3) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(4),
                            editMode = editMode,
                            onClick = { toggleSwitch(4) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionPlateKey(
                            label = "All Off",
                            icon = Icons.Rounded.PowerOff,
                            accentColor = LuminaColors.EmergencyRed,
                            borderColor = LuminaColors.EmergencyBorder.copy(alpha = 0.6f),
                            onClick = { setAll(false) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(7),
                            editMode = editMode,
                            onClick = { toggleSwitch(7) },
                            modifier = Modifier.weight(1f)
                        )
                        SwitchBoardKey(
                            sw = getSwitch(8),
                            editMode = editMode,
                            onClick = { toggleSwitch(8) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    editingSwitch?.let { target ->
        SwitchEditorDialog(
            sw = target,
            onDismiss = { editingSwitch = null },
            onSave = { updated ->
                saveBoard(
                    board.copy(
                        switches = board.switches.map {
                            if (it.number == updated.number) updated else it
                        }
                    )
                )
                editingSwitch = null
            }
        )
    }

    if (editingStepUrls) {
        StepUrlDialog(
            board = board,
            onDismiss = { editingStepUrls = false },
            onSave = { plus, minus, plusIr, minusIr, allOnIr, allOffIr ->
                saveBoard(
                    board.copy(
                        plusUrl = plus,
                        minusUrl = minus,
                        plusIrCode = plusIr,
                        minusIrCode = minusIr,
                        allOnIrCode = allOnIr,
                        allOffIrCode = allOffIr
                    )
                )
                editingStepUrls = false
            }
        )
    }
}

@Composable
private fun SwitchBoardKey(
    sw: HomeSwitch,
    editMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAmber = sw.type == SwitchType.LIGHT
    val activeBorder = if (isAmber) LuminaColors.AmberSignal else LuminaColors.CyanSignal
    val activeText = if (isAmber) LuminaColors.AmberText else LuminaColors.CyanText
    val icon = when (sw.type) {
        SwitchType.LIGHT -> Icons.Rounded.Lightbulb
        SwitchType.FAN -> Icons.Rounded.WindPower
        SwitchType.AC -> Icons.Rounded.AcUnit
        SwitchType.PLUG -> Icons.Rounded.Power
    }

    Surface(
        modifier = modifier
            .height(104.dp)
            .shadow(
                elevation = if (sw.isOn) 6.dp else 1.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = if (sw.isOn) activeBorder.copy(alpha = 0.38f) else Color.Black.copy(alpha = 0.05f),
                spotColor = if (sw.isOn) activeBorder.copy(alpha = 0.38f) else Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (sw.isOn) LuminaColors.TileOn else LuminaColors.TileOff,
        border = BorderStroke(
            width = if (editMode || sw.isOn) 1.5.dp else 1.dp,
            color = if (editMode) LuminaColors.CyanText else if (sw.isOn) activeBorder else LuminaColors.BorderOff
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${sw.number}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuminaColors.TextMuted
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (sw.isOn) activeBorder else LuminaColors.TileOn)
                        .border(
                            1.dp,
                            if (sw.isOn) activeBorder else LuminaColors.BorderOff,
                            CircleShape
                        )
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = sw.name,
                tint = if (sw.isOn) activeText else LuminaColors.TextMuted,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = sw.name,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = if (sw.isOn) FontWeight.SemiBold else FontWeight.Medium,
                color = if (sw.isOn) LuminaColors.TextPrimary else LuminaColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionPlateKey(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(104.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwitchEditorDialog(
    sw: HomeSwitch,
    onDismiss: () -> Unit,
    onSave: (HomeSwitch) -> Unit
) {
    var name by remember { mutableStateOf(sw.name) }
    var type by remember { mutableStateOf(sw.type) }
    var irCode by remember { mutableStateOf(sw.irCode) }
    var onUrl by remember { mutableStateOf(sw.onUrl) }
    var offUrl by remember { mutableStateOf(sw.offUrl) }
    var typeMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Edit Switch #${sw.number}", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Light / Appliance Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = typeMenu,
                    onExpandedChange = { typeMenu = it }
                ) {
                    OutlinedTextField(
                        value = type.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Icon Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                        SwitchType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.label) },
                                onClick = { type = t; typeMenu = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = irCode,
                    onValueChange = { irCode = it },
                    label = { Text("IR Hex Code (NEC 38kHz)") },
                    placeholder = { Text("0x00FF0${sw.number}FE") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = onUrl,
                    onValueChange = { onUrl = it },
                    label = { Text("Wi-Fi ON URL (Optional)") },
                    placeholder = { Text("http://192.168.1.50/sw${sw.number}/on") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = offUrl,
                    onValueChange = { offUrl = it },
                    label = { Text("Wi-Fi OFF URL (Optional)") },
                    placeholder = { Text("http://192.168.1.50/sw${sw.number}/off") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    onSave(
                        sw.copy(
                            name = name.trim(),
                            type = type,
                            irCode = irCode.trim(),
                            onUrl = onUrl.trim(),
                            offUrl = offUrl.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuminaColors.TextPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StepUrlDialog(
    board: SwitchboardState,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var pUrl by remember { mutableStateOf(board.plusUrl) }
    var mUrl by remember { mutableStateOf(board.minusUrl) }
    var pIr by remember { mutableStateOf(board.plusIrCode) }
    var mIr by remember { mutableStateOf(board.minusIrCode) }
    var allOnIr by remember { mutableStateOf(board.allOnIrCode) }
    var allOffIr by remember { mutableStateOf(board.allOffIrCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Configure + / - & All Keys", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = pIr,
                    onValueChange = { pIr = it },
                    label = { Text("[ + ] IR Hex Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mIr,
                    onValueChange = { mIr = it },
                    label = { Text("[ - ] IR Hex Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = allOnIr,
                    onValueChange = { allOnIr = it },
                    label = { Text("[ All On ] IR Hex Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = allOffIr,
                    onValueChange = { allOffIr = it },
                    label = { Text("[ All Off ] IR Hex Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pUrl,
                    onValueChange = { pUrl = it },
                    label = { Text("[ + ] Wi-Fi URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mUrl,
                    onValueChange = { mUrl = it },
                    label = { Text("[ - ] Wi-Fi URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        pUrl.trim(),
                        mUrl.trim(),
                        pIr.trim(),
                        mIr.trim(),
                        allOnIr.trim(),
                        allOffIr.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuminaColors.TextPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
