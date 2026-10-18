package com.example.data

enum class BlurType(val label: String, val pythonValue: String, val description: String) {
    GAUSSIAN("Gaussian Blur", "gaussian", "Smooth mathematical Gaussian kernel blur"),
    PIXELATE("Pixelate / Mosaic", "pixelate", "Downsamples & upsamples region into retro censor blocks"),
    MEDIAN("Median Filter", "median", "Reduces salt-and-pepper noise and blends textures"),
    BLACKOUT("Blackout Box", "blackout", "Completely conceals region with a solid dark mask")
}

enum class LogoStyle(val title: String, val tag: String) {
    CYBER_PULSE("Cyber Nexus", "Hexagonal tech glyph"),
    MINIMAL_STAR("Starlight Media", "Sleek geometric four-point star"),
    STUDIO_BADGE("Apex Studio", "Modern brand badge with typography"),
    SHIELD_CREATOR("Creator Seal", "Verified creator emblem with inner crown")
}

data class VideoResolution(
    val name: String,
    val width: Int,
    val height: Int,
    val aspectLabel: String
) {
    companion object {
        val RES_1080P = VideoResolution("1080p Full HD (16:9)", 1920, 1080, "16:9")
        val RES_720P = VideoResolution("720p HD (16:9)", 1280, 720, "16:9")
        val RES_VERTICAL = VideoResolution("Reels / TikTok (9:16)", 1080, 1920, "9:16")
        val RES_4K = VideoResolution("4K Ultra HD (16:9)", 3840, 2160, "16:9")
        val ALL = listOf(RES_1080P, RES_720P, RES_VERTICAL, RES_4K)
    }
}

data class BlurConfig(
    val enabled: Boolean = true,
    val x: Int = 1450,
    val y: Int = 920,
    val width: Int = 420,
    val height: Int = 120,
    val blurType: BlurType = BlurType.GAUSSIAN,
    val strength: Int = 41
)

data class LogoConfig(
    val enabled: Boolean = true,
    val x: Int = 1480,
    val y: Int = 930,
    val scale: Float = 0.55f,
    val opacity: Float = 0.95f,
    val style: LogoStyle = LogoStyle.CYBER_PULSE,
    val customImageUri: String? = null
)

data class WatermarkPreset(
    val name: String,
    val subtitle: String,
    val blurConfig: BlurConfig,
    val logoConfig: LogoConfig
)

enum class StudioTab(val label: String) {
    EDITOR("Canvas Editor"),
    BLUR_SETTINGS("Blur & Mask"),
    LOGO_SETTINGS("Logo Overlay"),
    STREAMLIT_CODE("Python & Deploy"),
    PRESETS("Presets")
}
