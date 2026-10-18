package com.example.ui.components

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
import com.example.data.LogoConfig
import com.example.data.LogoStyle
import com.example.data.VideoResolution
import com.example.ui.theme.*

@Composable
fun LogoControls(
    resolution: VideoResolution,
    blurConfig: BlurConfig,
    config: LogoConfig,
    onChange: (LogoConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("logo_controls_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle Active
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
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Logo overlay",
                        tint = AmberAccent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Transparent PNG Logo Overlay",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = "Composite watermark or brand mark with alpha blending",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { onChange(config.copy(enabled = it)) },
                    modifier = Modifier.testTag("logo_overlay_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberAccent,
                        checkedTrackColor = AmberAccent.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Quick Replacement / Snap Actions
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "PLACEMENT & WATERMARK REPLACEMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Highlighted button: Replace Blurred Watermark exactly!
                Button(
                    onClick = {
                        // Position inside or directly over the blurred bounding box
                        onChange(
                            config.copy(
                                x = blurConfig.x + 20,
                                y = blurConfig.y + 10
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("snap_to_watermark_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black)
                ) {
                    Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = "Snap", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Snap Directly Over Blurred Watermark", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val nx = resolution.width - 320
                            val ny = 30
                            onChange(config.copy(x = nx, y = ny))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Top-Right", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val nx = 30
                            val ny = 30
                            onChange(config.copy(x = nx, y = ny))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Top-Left", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val nx = (resolution.width - 300) / 2
                            val ny = (resolution.height - 150) / 2
                            onChange(config.copy(x = nx, y = ny))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Center", fontSize = 12.sp)
                    }
                }
            }
        }

        // Logo Style / Vector Brand Selector
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SELECT EMBEDDED PNG LOGO STYLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                LogoStyle.values().forEach { style ->
                    val isSelected = config.style == style
                    Surface(
                        onClick = { onChange(config.copy(style = style)) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AmberAccent.copy(alpha = 0.15f) else Color.Transparent,
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberAccent)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onChange(config.copy(style = style)) },
                                colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = style.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) AmberAccent else TextPrimary
                                    )
                                )
                                Text(
                                    text = style.tag,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Size & Opacity Controls
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "TRANSFORM & BLENDING (Alpha Compositing)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Scale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Scale Factor", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    Text(
                        text = "${String.format("%.2f", config.scale)}x",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                Slider(
                    value = config.scale,
                    onValueChange = { onChange(config.copy(scale = it)) },
                    valueRange = 0.1f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent),
                    modifier = Modifier.testTag("logo_scale_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Opacity (Alpha Channel)", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    Text(
                        text = "${(config.opacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                Slider(
                    value = config.opacity,
                    onValueChange = { onChange(config.copy(opacity = it)) },
                    valueRange = 0.05f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent),
                    modifier = Modifier.testTag("logo_opacity_slider")
                )
            }
        }

        // X & Y Offset
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "MANUAL COORDINATES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("X Offset: ${config.x} px", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        Slider(
                            value = config.x.toFloat().coerceIn(0f, resolution.width.toFloat()),
                            onValueChange = { onChange(config.copy(x = it.toInt())) },
                            valueRange = 0f..resolution.width.toFloat(),
                            colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Y Offset: ${config.y} px", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        Slider(
                            value = config.y.toFloat().coerceIn(0f, resolution.height.toFloat()),
                            onValueChange = { onChange(config.copy(y = it.toInt())) },
                            valueRange = 0f..resolution.height.toFloat(),
                            colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent)
                        )
                    }
                }
            }
        }
    }
}
