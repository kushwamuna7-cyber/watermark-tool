package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BlurConfig
import com.example.data.BlurType
import com.example.data.VideoResolution
import com.example.ui.theme.*

@Composable
fun WatermarkControls(
    resolution: VideoResolution,
    config: BlurConfig,
    onChange: (BlurConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("watermark_controls_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BlurOn,
                        contentDescription = "Blur icon",
                        tint = CyanAccent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Watermark Blur Active",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = "Obfuscate existing watermark in bounding box",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { onChange(config.copy(enabled = it)) },
                    modifier = Modifier.testTag("watermark_blur_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanAccent,
                        checkedTrackColor = CyanAccent.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Quick Preset Placements
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "QUICK CORNER PRESETS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val defaultW = (resolution.width * 0.22f).toInt()
                    val defaultH = (resolution.height * 0.12f).toInt()

                    PresetCornerButton(
                        label = "Bottom-Right",
                        icon = Icons.Default.CallReceived,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val nx = resolution.width - defaultW - 30
                            val ny = resolution.height - defaultH - 30
                            onChange(config.copy(x = nx, y = ny, width = defaultW, height = defaultH))
                        }
                    )
                    PresetCornerButton(
                        label = "Top-Right",
                        icon = Icons.Default.CallMade,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val nx = resolution.width - defaultW - 30
                            val ny = 30
                            onChange(config.copy(x = nx, y = ny, width = defaultW, height = defaultH))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val defaultW = (resolution.width * 0.22f).toInt()
                    val defaultH = (resolution.height * 0.12f).toInt()

                    PresetCornerButton(
                        label = "Bottom-Left",
                        icon = Icons.Default.SouthWest,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val nx = 30
                            val ny = resolution.height - defaultH - 30
                            onChange(config.copy(x = nx, y = ny, width = defaultW, height = defaultH))
                        }
                    )
                    PresetCornerButton(
                        label = "Top-Left",
                        icon = Icons.Default.NorthWest,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val nx = 30
                            val ny = 30
                            onChange(config.copy(x = nx, y = ny, width = defaultW, height = defaultH))
                        }
                    )
                }
            }
        }

        // Blur Algorithm Selector
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "BLUR ALGORITHM (OpenCV Filter)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                BlurType.values().forEach { blurType ->
                    val isSelected = config.blurType == blurType
                    Surface(
                        onClick = { onChange(config.copy(blurType = blurType)) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CyanAccent.copy(alpha = 0.15f) else Color.Transparent,
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanAccent)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("blur_type_${blurType.pythonValue}")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onChange(config.copy(blurType = blurType)) },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = blurType.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) CyanAccent else TextPrimary
                                    )
                                )
                                Text(
                                    text = blurType.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Blur Strength Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Blur Strength / Kernel Radius",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                    Text(
                        text = "${config.strength} px",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                Slider(
                    value = config.strength.toFloat(),
                    onValueChange = { onChange(config.copy(strength = it.toInt())) },
                    valueRange = 5f..101f,
                    steps = 47,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                    modifier = Modifier.testTag("blur_strength_slider")
                )
            }
        }

        // Numerical Coordinate Fine-Tuning
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "COORDINATE PRECISION (Pixels)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CoordinateSlider(
                        label = "X Offset",
                        value = config.x,
                        max = resolution.width,
                        modifier = Modifier.weight(1f),
                        onValueChange = { onChange(config.copy(x = it)) }
                    )
                    CoordinateSlider(
                        label = "Y Offset",
                        value = config.y,
                        max = resolution.height,
                        modifier = Modifier.weight(1f),
                        onValueChange = { onChange(config.copy(y = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CoordinateSlider(
                        label = "Width",
                        value = config.width,
                        max = resolution.width,
                        modifier = Modifier.weight(1f),
                        onValueChange = { onChange(config.copy(width = it)) }
                    )
                    CoordinateSlider(
                        label = "Height",
                        value = config.height,
                        max = resolution.height,
                        modifier = Modifier.weight(1f),
                        onValueChange = { onChange(config.copy(height = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCornerButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(8.dp),
        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioCardBorder)),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = CyanAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary))
    }
}

@Composable
private fun CoordinateSlider(
    label: String,
    value: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            Text(
                text = "$value px",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyanAccent
                )
            )
        }
        Slider(
            value = value.toFloat().coerceIn(0f, max.toFloat()),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..max.toFloat(),
            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
        )
    }
}
