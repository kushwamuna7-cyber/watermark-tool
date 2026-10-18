package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BlurConfig
import com.example.data.BlurType
import com.example.data.LogoConfig
import com.example.data.LogoStyle
import com.example.data.VideoResolution
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VideoCanvasEditor(
    resolution: VideoResolution,
    blurConfig: BlurConfig,
    logoConfig: LogoConfig,
    onBlurChange: (BlurConfig) -> Unit,
    onLogoChange: (LogoConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var showComparison by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0.42f) }
    var selectedLayer by remember { mutableStateOf("watermark") } // "watermark" or "logo"

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_canvas_editor_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(StudioCardBorder, Color(0xFF1E293B))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (blurConfig.enabled) CyanAccent else AmberAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE VIDEO STAGE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${resolution.width}x${resolution.height})",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = showComparison,
                        onClick = { showComparison = !showComparison },
                        label = { Text("Compare Split", style = TextStyle(fontSize = 11.sp)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Compare,
                                contentDescription = "Toggle comparison",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoAccent.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("toggle_compare_button")
                    )

                    FilterChip(
                        selected = selectedLayer == "logo",
                        onClick = {
                            selectedLayer = if (selectedLayer == "logo") "watermark" else "logo"
                        },
                        label = { Text(if (selectedLayer == "logo") "Editing: Logo" else "Editing: Watermark", style = TextStyle(fontSize = 11.sp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberAccent.copy(alpha = 0.25f),
                            selectedLabelColor = AmberAccent
                        ),
                        modifier = Modifier.testTag("toggle_layer_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Aspect Ratio Container for Canvas
            val canvasAspectRatio = resolution.width.toFloat() / resolution.height.toFloat()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(canvasAspectRatio.coerceIn(0.6f, 2.0f))
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                    .background(Color(0xFF070A10))
            ) {
                // Interactive Compose Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("interactive_video_canvas")
                        .pointerInput(resolution, blurConfig, logoConfig, selectedLayer) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val canvasW = size.width
                                val canvasH = size.height
                                val scaleX = resolution.width.toFloat() / canvasW
                                val scaleY = resolution.height.toFloat() / canvasH

                                val deltaVideoX = (dragAmount.x * scaleX).toInt()
                                val deltaVideoY = (dragAmount.y * scaleY).toInt()

                                if (selectedLayer == "watermark") {
                                    val newX = (blurConfig.x + deltaVideoX).coerceIn(0, resolution.width - blurConfig.width)
                                    val newY = (blurConfig.y + deltaVideoY).coerceIn(0, resolution.height - blurConfig.height)
                                    onBlurChange(blurConfig.copy(x = newX, y = newY))
                                } else {
                                    val newX = (logoConfig.x + deltaVideoX).coerceIn(0, resolution.width - 200)
                                    val newY = (logoConfig.y + deltaVideoY).coerceIn(0, resolution.height - 100)
                                    onLogoChange(logoConfig.copy(x = newX, y = newY))
                                }
                            }
                        }
                ) {
                    val canvasW = size.width
                    val canvasH = size.height

                    val scaleX = canvasW / resolution.width.toFloat()
                    val scaleY = canvasH / resolution.height.toFloat()

                    // 1. Draw Simulated Video Scene
                    drawSimulatedVideoScene(canvasW, canvasH, scrubPosition, textMeasurer)

                    // 2. Draw Split divider if in comparison mode
                    val splitX = if (showComparison) canvasW * 0.5f else canvasW

                    // 3. Draw Watermark Blur on the right/active side
                    if (blurConfig.enabled) {
                        val bx = blurConfig.x * scaleX
                        val by = blurConfig.y * scaleY
                        val bw = blurConfig.width * scaleX
                        val bh = blurConfig.height * scaleY

                        // Apply simulated blur effect inside the rect
                        drawBlurEffect(
                            bx = bx,
                            by = by,
                            bw = bw,
                            bh = bh,
                            blurType = blurConfig.blurType,
                            strength = blurConfig.strength
                        )

                        // Draw bounding box wireframe & handles
                        val boxColor = if (selectedLayer == "watermark") CyanAccent else CyanAccent.copy(alpha = 0.5f)
                        drawRoundRect(
                            color = boxColor,
                            topLeft = Offset(bx, by),
                            size = Size(bw, bh),
                            cornerRadius = CornerRadius(4f, 4f),
                            style = Stroke(
                                width = if (selectedLayer == "watermark") 3f else 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                            )
                        )

                        // Draw resize grip handle at bottom-right
                        drawCircle(
                            color = HandleColor,
                            radius = 6.dp.toPx(),
                            center = Offset(bx + bw, by + bh)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 3.dp.toPx(),
                            center = Offset(bx + bw, by + bh)
                        )

                        // Label HUD above box
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "WATERMARK BLUR: [${blurConfig.x}, ${blurConfig.y} | ${blurConfig.width}x${blurConfig.height}]",
                            topLeft = Offset(bx, (by - 18.dp.toPx()).coerceAtLeast(4f)),
                            style = TextStyle(
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                background = Color(0xCC000000)
                            )
                        )
                    }

                    // 4. Draw Logo Overlay
                    if (logoConfig.enabled) {
                        val lx = logoConfig.x * scaleX
                        val ly = logoConfig.y * scaleY
                        val logoBaseW = 280f * scaleX * logoConfig.scale
                        val logoBaseH = 140f * scaleY * logoConfig.scale

                        drawLogoLayer(
                            lx = lx,
                            ly = ly,
                            lw = logoBaseW,
                            lh = logoBaseH,
                            style = logoConfig.style,
                            opacity = logoConfig.opacity,
                            isSelected = (selectedLayer == "logo"),
                            textMeasurer = textMeasurer
                        )
                    }

                    // 5. Draw split comparison bar
                    if (showComparison) {
                        drawLine(
                            color = Color.White,
                            start = Offset(splitX, 0f),
                            end = Offset(splitX, canvasH),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "ORIGINAL",
                            topLeft = Offset(12f, 12f),
                            style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, background = Color(0x99000000))
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "PROCESSED",
                            topLeft = Offset(splitX + 12f, 12f),
                            style = TextStyle(color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, background = Color(0x99000000))
                        )
                    }
                }

                // Touch hint badge at bottom-left
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Drag to reposition",
                            tint = if (selectedLayer == "watermark") CyanAccent else AmberAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedLayer == "watermark") "Drag watermark box to adjust X/Y" else "Drag logo overlay to adjust X/Y",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Video Time Scrubber & Quick Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Simulated play",
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "00:${String.format("%02d", (scrubPosition * 30).toInt())}:18",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = TextSecondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = scrubPosition,
                    onValueChange = { scrubPosition = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("timeline_scrubber"),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = StudioCardBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "00:30:00",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = TextMuted)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// CANVAS DRAWING HELPERS
// -------------------------------------------------------------------------

private fun DrawScope.drawSimulatedVideoScene(
    w: Float,
    h: Float,
    scrub: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    // Dynamic cinematic gradient background simulating video footage
    val hueShift = scrub * 40f
    val gradBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF1E1B4B),
            Color(0xFF042F2E)
        ),
        start = Offset(0f, 0f),
        end = Offset(w, h)
    )
    drawRect(brush = gradBrush, size = Size(w, h))

    // Subtle perspective grid lines simulating 3D video content
    val gridPaint = Color(0x1A00E5FF)
    val numLines = 8
    for (i in 0..numLines) {
        val y = h * (i.toFloat() / numLines)
        drawLine(color = gridPaint, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
    }

    // Draw simulated original unwanted watermark in video (e.g. bottom-right default area)
    drawText(
        textMeasurer = textMeasurer,
        text = "🎬 @ORIGINAL_WATERMARK_2024",
        topLeft = Offset(w * 0.72f, h * 0.83f),
        style = TextStyle(
            color = Color(0x99FFFFFF),
            fontSize = (11f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )

    // Center focal object (geometric cinematic camera glyph)
    val cx = w * 0.5f + (sin(scrub * 6.28) * 15f).toFloat()
    val cy = h * 0.5f
    drawCircle(color = Color(0x22FFFFFF), radius = h * 0.22f, center = Offset(cx, cy))
    drawCircle(color = Color(0x4400E5FF), radius = h * 0.12f, center = Offset(cx, cy))
}

private fun DrawScope.drawBlurEffect(
    bx: Float,
    by: Float,
    bw: Float,
    bh: Float,
    blurType: BlurType,
    strength: Int
) {
    when (blurType) {
        BlurType.GAUSSIAN, BlurType.MEDIAN -> {
            // Simulated gaussian diffusion: frosted layered backdrop + dense blurred dot grid
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xCC1E293B), Color(0xEE0F172A)),
                    center = Offset(bx + bw / 2, by + bh / 2),
                    radius = bw
                ),
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Texture noise dots to simulate smoothed Gaussian dispersion
            val rows = 6
            val cols = 12
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val px = bx + (bw * (c.toFloat() / cols)) + 4f
                    val py = by + (bh * (r.toFloat() / rows)) + 4f
                    drawCircle(
                        color = Color(0x3394A3B8),
                        radius = (strength / 20f).coerceIn(2f, 6f),
                        center = Offset(px, py)
                    )
                }
            }
        }

        BlurType.PIXELATE -> {
            // Mosaic / pixelation blocks
            val blockSize = (strength / 3).coerceIn(8, 28).toFloat()
            val cols = (bw / blockSize).toInt().coerceAtLeast(1)
            val rows = (bh / blockSize).toInt().coerceAtLeast(1)

            val colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF334155),
                Color(0xFF0F172A),
                Color(0xFF475569)
            )

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val px = bx + c * blockSize
                    val py = by + r * blockSize
                    val colIdx = (r * 3 + c * 7) % colors.size
                    drawRect(
                        color = colors[colIdx],
                        topLeft = Offset(px, py),
                        size = Size(blockSize, blockSize)
                    )
                    drawRect(
                        color = Color(0x15000000),
                        topLeft = Offset(px, py),
                        size = Size(blockSize, blockSize),
                        style = Stroke(1f)
                    )
                }
            }
        }

        BlurType.BLACKOUT -> {
            // Solid matte black box with subtle hazard stripes
            drawRect(
                color = Color(0xFF0A0A0A),
                topLeft = Offset(bx, by),
                size = Size(bw, bh)
            )
            val lineSpacing = 16f
            var lx = bx
            while (lx < bx + bw) {
                drawLine(
                    color = Color(0x22F59E0B),
                    start = Offset(lx, by),
                    end = Offset(lx + bh, by + bh),
                    strokeWidth = 2f
                )
                lx += lineSpacing
            }
        }
    }
}

private fun DrawScope.drawLogoLayer(
    lx: Float,
    ly: Float,
    lw: Float,
    lh: Float,
    style: LogoStyle,
    opacity: Float,
    isSelected: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val alphaColor = Color.White.copy(alpha = opacity)
    val accentAlpha = CyanAccent.copy(alpha = opacity)

    // Selection wireframe if active layer
    if (isSelected) {
        drawRoundRect(
            color = AmberAccent,
            topLeft = Offset(lx - 4f, ly - 4f),
            size = Size(lw + 8f, lh + 8f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f))
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "NEW LOGO OVERLAY",
            topLeft = Offset(lx, ly - 16.dp.toPx()),
            style = TextStyle(color = AmberAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        )
    }

    when (style) {
        LogoStyle.CYBER_PULSE -> {
            // Hexagon tech logo
            val cx = lx + lw * 0.25f
            val cy = ly + lh * 0.5f
            val rad = lh * 0.4f

            val path = Path()
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60.0) - 30.0)
                val x = cx + (rad * cos(angle)).toFloat()
                val y = cy + (rad * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(path = path, color = accentAlpha, style = Stroke(width = 3f))
            drawCircle(color = accentAlpha, radius = rad * 0.35f, center = Offset(cx, cy))

            drawText(
                textMeasurer = textMeasurer,
                text = "CYBER//NEXUS",
                topLeft = Offset(lx + lw * 0.48f, ly + lh * 0.35f),
                style = TextStyle(
                    color = alphaColor,
                    fontSize = (lh * 0.25f).coerceIn(10f, 16f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            )
        }

        LogoStyle.MINIMAL_STAR -> {
            // Geometric 4-point star
            val cx = lx + lw * 0.25f
            val cy = ly + lh * 0.5f
            val rMax = lh * 0.45f
            val rMin = rMax * 0.3f

            val path = Path()
            for (i in 0 until 8) {
                val angle = Math.toRadians(i * 45.0)
                val r = if (i % 2 == 0) rMax else rMin
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(path = path, color = accentAlpha)

            drawText(
                textMeasurer = textMeasurer,
                text = "STARLIGHT",
                topLeft = Offset(lx + lw * 0.48f, ly + lh * 0.35f),
                style = TextStyle(
                    color = alphaColor,
                    fontSize = (lh * 0.25f).coerceIn(10f, 16f).sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        LogoStyle.STUDIO_BADGE -> {
            // Apex Studio Badge
            drawRoundRect(
                color = accentAlpha,
                topLeft = Offset(lx, ly),
                size = Size(lw, lh),
                cornerRadius = CornerRadius(lh * 0.2f, lh * 0.2f),
                style = Stroke(2f)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "APEX STUDIO",
                topLeft = Offset(lx + lw * 0.15f, ly + lh * 0.35f),
                style = TextStyle(
                    color = alphaColor,
                    fontSize = (lh * 0.26f).coerceIn(10f, 16f).sp,
                    fontWeight = FontWeight.Black
                )
            )
        }

        LogoStyle.SHIELD_CREATOR -> {
            // Creator Seal
            val cx = lx + lw * 0.22f
            val cy = ly + lh * 0.5f
            val r = lh * 0.4f
            drawCircle(color = accentAlpha, radius = r, center = Offset(cx, cy), style = Stroke(2.5f))
            drawCircle(color = Color(0xFFF59E0B).copy(alpha = opacity), radius = r * 0.45f, center = Offset(cx, cy))

            drawText(
                textMeasurer = textMeasurer,
                text = "VERIFIED",
                topLeft = Offset(lx + lw * 0.45f, ly + lh * 0.35f),
                style = TextStyle(
                    color = alphaColor,
                    fontSize = (lh * 0.24f).coerceIn(10f, 16f).sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
