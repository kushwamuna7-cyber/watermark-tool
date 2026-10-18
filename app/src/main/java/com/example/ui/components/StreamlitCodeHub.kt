package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BlurConfig
import com.example.data.LogoConfig
import com.example.data.VideoResolution
import com.example.ui.theme.*

@Composable
fun StreamlitCodeHub(
    resolution: VideoResolution,
    blurConfig: BlurConfig,
    logoConfig: LogoConfig,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFileTab by remember { mutableIntStateOf(0) }
    val fileNames = listOf("app.py", "requirements.txt", "Dockerfile", "Deploy Guide")

    val pythonCode = remember(resolution, blurConfig, logoConfig) {
        generatePythonCode(resolution, blurConfig, logoConfig)
    }

    val requirementsContent = """
streamlit>=1.36.0
opencv-python-headless>=4.8.0.76
moviepy>=1.0.3
numpy>=1.24.0
Pillow>=10.0.0
""".trimIndent()

    val dockerContent = """
FROM python:3.10-slim

WORKDIR /app

# Install system libraries for OpenCV and FFmpeg
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    libsm6 \
    libxext6 \
    libgl1 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app.py .

EXPOSE 8501

ENTRYPOINT ["streamlit", "run", "app.py", "--server.port=8501", "--server.address=0.0.0.0"]
""".trimIndent()

    val deployGuideContent = """
# Quick Deployment Guide

### Option A: Local Run (Fastest)
1. Install Python 3.9+
2. Install dependencies:
   pip install -r requirements.txt
3. Run the Streamlit web app:
   streamlit run app.py
4. Open your browser at http://localhost:8501

### Option B: Streamlit Community Cloud (Free Hosting)
1. Push app.py and requirements.txt to a GitHub repository.
2. Visit https://share.streamlit.io and connect your GitHub account.
3. Select your repository, branch: main, and Main file path: app.py.
4. Click 'Deploy' - Streamlit automatically sets up environment!

### Option C: Docker Container
1. Build container:
   docker build -t watermark-studio .
2. Run container:
   docker run -p 8501:8501 watermark-studio
""".trimIndent()

    val activeContent = when (selectedFileTab) {
        0 -> pythonCode
        1 -> requirementsContent
        2 -> dockerContent
        else -> deployGuideContent
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("code_hub_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = "Terminal", tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Streamlit & OpenCV Engine",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(fileNames[selectedFileTab], activeContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied ${fileNames[selectedFileTab]} to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("copy_code_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy File", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Full clean source code configured with your live coordinates: Blur X=${blurConfig.x}, Y=${blurConfig.y}, W=${blurConfig.width}, H=${blurConfig.height}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Sub-tabs row
        ScrollableTabRow(
            selectedTabIndex = selectedFileTab,
            containerColor = StudioSurface,
            contentColor = CyanAccent,
            edgePadding = 0.dp
        ) {
            fileNames.forEachIndexed { index, name ->
                Tab(
                    selected = selectedFileTab == index,
                    onClick = { selectedFileTab = index },
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (selectedFileTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (selectedFileTab == index) CyanAccent else TextMuted
                        )
                    }
                )
            }
        }

        // Code Viewer Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF050811)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioCardBorder))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileNames[selectedFileTab],
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = AmberAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${activeContent.lines().size} lines",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 460.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0F1D))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = activeContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                                color = TextPrimary
                            )
                        )
                    }
                }
            }
        }

        // Deployment Status Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ready",
                    tint = GreenAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "app.py & requirements.txt Generated",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Files are saved in your project root ready to deploy on Streamlit Cloud, AWS, or Docker.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// DYNAMIC CODE GENERATOR
// -------------------------------------------------------------------------
private fun generatePythonCode(
    res: VideoResolution,
    blur: BlurConfig,
    logo: LogoConfig
): String {
    return """
import tempfile
import cv2
import numpy as np
from PIL import Image
import streamlit as st
from moviepy.editor import VideoFileClip

st.set_page_config(page_title="Video Watermark & Logo Studio", layout="wide")
st.title("🎬 Video Watermark Blur & PNG Logo Overlay Studio")

# -------------------------------------------------------------
# 1. WATERMARK BLUR ENGINE (OpenCV)
# -------------------------------------------------------------
def apply_watermark_blur(frame, x=${blur.x}, y=${blur.y}, w=${blur.width}, h=${blur.height}, blur_type="${blur.blurType.pythonValue}", strength=${blur.strength}):
    h_frame, w_frame = frame.shape[:2]
    x1, y1 = max(0, int(x)), max(0, int(y))
    x2, y2 = min(w_frame, int(x + w)), min(h_frame, int(y + h))
    if x2 <= x1 or y2 <= y1:
        return frame

    roi = frame[y1:y2, x1:x2]
    if blur_type == "gaussian":
        k = strength if strength % 2 == 1 else strength + 1
        blurred = cv2.GaussianBlur(roi, (max(3, k), max(3, k)), 0)
    elif blur_type == "pixelate":
        scale = max(2, strength // 4)
        sh, sw = max(1, roi.shape[0] // scale), max(1, roi.shape[1] // scale)
        small = cv2.resize(roi, (sw, sh), interpolation=cv2.INTER_LINEAR)
        blurred = cv2.resize(small, (roi.shape[1], roi.shape[0]), interpolation=cv2.INTER_NEAREST)
    elif blur_type == "median":
        k = strength if strength % 2 == 1 else strength + 1
        blurred = cv2.medianBlur(roi, max(3, k))
    elif blur_type == "blackout":
        blurred = np.zeros_like(roi)
    else:
        blurred = roi

    frame[y1:y2, x1:x2] = blurred
    return frame

# -------------------------------------------------------------
# 2. TRANSPARENT PNG LOGO OVERLAY (Alpha Blending)
# -------------------------------------------------------------
def overlay_transparent_logo(frame, logo_rgba, x=${logo.x}, y=${logo.y}, scale=${logo.scale}, opacity=${logo.opacity}):
    if logo_rgba is None:
        return frame
    h_frame, w_frame = frame.shape[:2]
    new_w = max(1, int(logo_rgba.shape[1] * scale))
    new_h = max(1, int(logo_rgba.shape[0] * scale))
    resized = cv2.resize(logo_rgba, (new_w, new_h), interpolation=cv2.INTER_AREA)

    fx1, fy1 = max(0, x), max(0, y)
    fx2, fy2 = min(w_frame, x + new_w), min(h_frame, y + new_h)
    if fx2 <= fx1 or fy2 <= fy1:
        return frame

    lx1, ly1 = fx1 - x, fy1 - y
    lx2, ly2 = lx1 + (fx2 - fx1), ly1 + (fy2 - fy1)
    crop = resized[ly1:ly2, lx1:lx2]

    alpha = (crop[:, :, 3] / 255.0 * opacity)[:, :, np.newaxis]
    bg = frame[fy1:fy2, fx1:fx2].astype(np.float32)
    fg = crop[:, :, :3].astype(np.float32)
    frame[fy1:fy2, fx1:fx2] = np.clip(alpha * fg + (1.0 - alpha) * bg, 0, 255).astype(np.uint8)
    return frame

# -------------------------------------------------------------
# 3. STREAMLIT UI & MOVIEPY PROCESSING
# -------------------------------------------------------------
uploaded_video = st.file_uploader("Upload Video", type=["mp4", "mov", "avi"])
uploaded_logo = st.file_uploader("Upload Transparent PNG Logo", type=["png", "webp"])

if uploaded_video:
    tfile = tempfile.NamedTemporaryFile(delete=False, suffix=".mp4")
    tfile.write(uploaded_video.read())
    clip = VideoFileClip(tfile.name)
    logo_rgba = np.array(Image.open(uploaded_logo).convert("RGBA")) if uploaded_logo else None

    # Interactive coordinates controls
    col1, col2 = st.columns(2)
    with col1:
        bx = st.number_input("Watermark X", value=${blur.x})
        by = st.number_input("Watermark Y", value=${blur.y})
    with col2:
        bw = st.number_input("Watermark Width", value=${blur.width})
        bh = st.number_input("Watermark Height", value=${blur.height})

    if st.button("🚀 Render Processed Video"):
        def transform_frame(frame):
            f = apply_watermark_blur(frame, bx, by, bw, bh, "${blur.blurType.pythonValue}", ${blur.strength})
            if logo_rgba is not None:
                f = overlay_transparent_logo(f, logo_rgba, ${logo.x}, ${logo.y}, ${logo.scale}, ${logo.opacity})
            return f

        out_clip = clip.fl_image(transform_frame)
        out_path = tempfile.mktemp(suffix=".mp4")
        out_clip.write_videofile(out_path, codec="libx264", audio_codec="aac")
        st.video(out_path)
""".trimIndent()
}
