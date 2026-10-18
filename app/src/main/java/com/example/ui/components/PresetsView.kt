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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun PresetsView(
    currentResolution: VideoResolution,
    onApplyPreset: (VideoResolution, BlurConfig, LogoConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        PresetItem(
            title = "TikTok / Reels Corner Watermark",
            description = "Blurs moving bottom-right creator watermark and places verified branding logo.",
            badge = "9:16 Vertical",
            res = VideoResolution.RES_VERTICAL,
            blur = BlurConfig(
                enabled = true,
                x = 650,
                y = 1600,
                width = 380,
                height = 140,
                blurType = BlurType.GAUSSIAN,
                strength = 45
            ),
            logo = LogoConfig(
                enabled = true,
                x = 660,
                y = 1610,
                scale = 0.5f,
                opacity = 0.95f,
                style = LogoStyle.CYBER_PULSE
            )
        ),
        PresetItem(
            title = "YouTube Video Branding",
            description = "Blurs default bottom-right channel watermark and overlays crisp geometric star logo.",
            badge = "1080p Standard",
            res = VideoResolution.RES_1080P,
            blur = BlurConfig(
                enabled = true,
                x = 1680,
                y = 880,
                width = 200,
                height = 160,
                blurType = BlurType.PIXELATE,
                strength = 35
            ),
            logo = LogoConfig(
                enabled = true,
                x = 1690,
                y = 890,
                scale = 0.45f,
                opacity = 1.0f,
                style = LogoStyle.MINIMAL_STAR
            )
        ),
        PresetItem(
            title = "Broadcast Lower-Third Chyron",
            description = "Conceals bottom banner ticker or timestamp and places high-contrast studio seal.",
            badge = "1080p Landscape",
            res = VideoResolution.RES_1080P,
            blur = BlurConfig(
                enabled = true,
                x = 60,
                y = 920,
                width = 750,
                height = 110,
                blurType = BlurType.GAUSSIAN,
                strength = 51
            ),
            logo = LogoConfig(
                enabled = true,
                x = 80,
                y = 930,
                scale = 0.55f,
                opacity = 0.9f,
                style = LogoStyle.STUDIO_BADGE
            )
        ),
        PresetItem(
            title = "Stock Video Center Obfuscator",
            description = "Applies strong mosaic pixelation over center demo watermark.",
            badge = "1080p Center",
            res = VideoResolution.RES_1080P,
            blur = BlurConfig(
                enabled = true,
                x = 680,
                y = 440,
                width = 560,
                height = 200,
                blurType = BlurType.PIXELATE,
                strength = 61
            ),
            logo = LogoConfig(
                enabled = true,
                x = 720,
                y = 470,
                scale = 0.65f,
                opacity = 0.85f,
                style = LogoStyle.SHIELD_CREATOR
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("presets_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "ONE-TAP PRODUCTION PRESETS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = TextSecondary
            )
        )

        presets.forEach { preset ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IndigoAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = preset.badge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = IndigoAccent, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Blur: ${preset.blur.width}x${preset.blur.height} px (${preset.blur.blurType.label})",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanAccent)
                        )

                        Button(
                            onClick = { onApplyPreset(preset.res, preset.blur, preset.logo) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                            modifier = Modifier.testTag("apply_preset_${preset.title.replace(" ", "_").lowercase()}")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Apply", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Preset", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class PresetItem(
    val title: String,
    val description: String,
    val badge: String,
    val res: VideoResolution,
    val blur: BlurConfig,
    val logo: LogoConfig
)
