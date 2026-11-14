package com.obsidianlumina.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Blinds
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WindPower
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obsidianlumina.home.model.DeviceType
import com.obsidianlumina.home.model.HomeDevice
import com.obsidianlumina.home.model.SignalTone
import com.obsidianlumina.home.ui.theme.LuminaColors
import java.util.Locale

@Composable
fun TouchTile(
    device: HomeDevice,
    roomName: String,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentSignal = when (device.signalTone) {
        SignalTone.CYAN -> LuminaColors.CyanSignal
        SignalTone.AMBER -> LuminaColors.AmberSignal
        SignalTone.EMERALD -> LuminaColors.EmeraldSignal
    }
    val accentText = when (device.signalTone) {
        SignalTone.CYAN -> LuminaColors.CyanText
        SignalTone.AMBER -> LuminaColors.AmberText
        SignalTone.EMERALD -> LuminaColors.EmeraldText
    }

    val containerBg = if (device.isOn) LuminaColors.TileOn else LuminaColors.TileOff
    val borderColor = if (device.isOn) accentSignal else LuminaColors.BorderOff

    val icon: ImageVector = when (device.type) {
        DeviceType.DIMMER -> Icons.Rounded.WbSunny
        DeviceType.LIGHT -> Icons.Rounded.Lightbulb
        DeviceType.FAN -> Icons.Rounded.WindPower
        DeviceType.CLIMATE -> Icons.Rounded.AcUnit
        DeviceType.BLIND -> Icons.Rounded.Blinds
        DeviceType.LOCK -> if (device.isOn) Icons.Rounded.Lock else Icons.Rounded.LockOpen
        DeviceType.PLUG -> Icons.Rounded.Power
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (device.isOn) 8.dp else 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (device.isOn) accentSignal.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.08f),
                spotColor = if (device.isOn) accentSignal.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (device.isOn) accentSignal.copy(alpha = 0.16f)
                            else LuminaColors.CanvasBase
                        )
                        .border(
                            width = 1.dp,
                            color = if (device.isOn) accentSignal.copy(alpha = 0.45f) else LuminaColors.Hairline,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = device.name,
                        tint = if (device.isOn) accentText else LuminaColors.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = device.channel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = LuminaColors.TextMuted
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (device.isOn) accentSignal else LuminaColors.TileOn)
                            .border(
                                width = 1.dp,
                                color = if (device.isOn) accentSignal else LuminaColors.BorderOff,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (device.isOn) LuminaColors.TextPrimary else LuminaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$roomName · ${device.secondaryLabel()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (device.isOn) accentText else LuminaColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f W", device.currentWatts()),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = LuminaColors.TextMuted
                    )
                }

                IconButton(
                    onClick = onOpenDetail,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Configure ${device.name}",
                        tint = LuminaColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
