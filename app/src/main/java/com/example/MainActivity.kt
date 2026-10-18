package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WatermarkStudioApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkStudioApp() {
    var activeTab by remember { mutableStateOf(StudioTab.EDITOR) }
    var currentResolution by remember { mutableStateOf(VideoResolution.RES_1080P) }
    var blurConfig by remember {
        mutableStateOf(
            BlurConfig(
                enabled = true,
                x = 1420,
                y = 890,
                width = 440,
                height = 130,
                blurType = BlurType.GAUSSIAN,
                strength = 41
            )
        )
    }
    var logoConfig by remember {
        mutableStateOf(
            LogoConfig(
                enabled = true,
                x = 1450,
                y = 900,
                scale = 0.55f,
                opacity = 0.95f,
                style = LogoStyle.CYBER_PULSE
            )
        )
    }

    var showResMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("watermark_studio_root"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanAccent.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = "App Icon",
                                tint = CyanAccent,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Watermark Studio",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "OpenCV & Streamlit Video Suite",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                },
                actions = {
                    // Resolution Dropdown Selector
                    Box {
                        OutlinedButton(
                            onClick = { showResMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonBorder().let {
                                ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("resolution_picker_button")
                        ) {
                            Text(currentResolution.aspectLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showResMenu,
                            onDismissRequest = { showResMenu = false },
                            modifier = Modifier.background(StudioSurface)
                        ) {
                            VideoResolution.ALL.forEach { res ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(res.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                            Text("${res.width} x ${res.height}", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                                        }
                                    },
                                    onClick = {
                                        currentResolution = res
                                        showResMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Quick Jump to Streamlit Python Tab
                    IconButton(
                        onClick = { activeTab = StudioTab.STREAMLIT_CODE },
                        modifier = Modifier.testTag("action_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Python Code Hub",
                            tint = if (activeTab == StudioTab.STREAMLIT_CODE) CyanAccent else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == StudioTab.EDITOR,
                    onClick = { activeTab = StudioTab.EDITOR },
                    icon = { Icon(Icons.Default.FitScreen, contentDescription = "Editor") },
                    label = { Text("Canvas", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_canvas")
                )

                NavigationBarItem(
                    selected = activeTab == StudioTab.BLUR_SETTINGS,
                    onClick = { activeTab = StudioTab.BLUR_SETTINGS },
                    icon = { Icon(Icons.Default.BlurOn, contentDescription = "Blur") },
                    label = { Text("Blur", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_blur")
                )

                NavigationBarItem(
                    selected = activeTab == StudioTab.LOGO_SETTINGS,
                    onClick = { activeTab = StudioTab.LOGO_SETTINGS },
                    icon = { Icon(Icons.Default.Layers, contentDescription = "Logo") },
                    label = { Text("Logo", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = AmberAccent,
                        indicatorColor = AmberAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_logo")
                )

                NavigationBarItem(
                    selected = activeTab == StudioTab.PRESETS,
                    onClick = { activeTab = StudioTab.PRESETS },
                    icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Presets") },
                    label = { Text("Presets", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = IndigoAccent,
                        indicatorColor = IndigoAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_presets")
                )

                NavigationBarItem(
                    selected = activeTab == StudioTab.STREAMLIT_CODE,
                    onClick = { activeTab = StudioTab.STREAMLIT_CODE },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Python") },
                    label = { Text("Python", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_python")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioBackground)
        ) {
            when (activeTab) {
                StudioTab.EDITOR -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        VideoCanvasEditor(
                            resolution = currentResolution,
                            blurConfig = blurConfig,
                            logoConfig = logoConfig,
                            onBlurChange = { blurConfig = it },
                            onLogoChange = { logoConfig = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick info cards beneath canvas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = StudioSurface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("WATERMARK BLUR", style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent, fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "${blurConfig.blurType.label} (${blurConfig.strength}px)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                    )
                                    Text(
                                        text = "X:${blurConfig.x} Y:${blurConfig.y} [${blurConfig.width}x${blurConfig.height}]",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = StudioSurface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("LOGO OVERLAY", style = MaterialTheme.typography.labelSmall.copy(color = AmberAccent, fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "${logoConfig.style.title} (${(logoConfig.opacity * 100).toInt()}% alpha)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                    )
                                    Text(
                                        text = "Scale: ${logoConfig.scale}x at X:${logoConfig.x} Y:${logoConfig.y}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                    )
                                }
                            }
                        }
                    }
                }

                StudioTab.BLUR_SETTINGS -> {
                    WatermarkControls(
                        resolution = currentResolution,
                        config = blurConfig,
                        onChange = { blurConfig = it }
                    )
                }

                StudioTab.LOGO_SETTINGS -> {
                    LogoControls(
                        resolution = currentResolution,
                        blurConfig = blurConfig,
                        config = logoConfig,
                        onChange = { logoConfig = it }
                    )
                }

                StudioTab.PRESETS -> {
                    PresetsView(
                        currentResolution = currentResolution,
                        onApplyPreset = { newRes, newBlur, newLogo ->
                            currentResolution = newRes
                            blurConfig = newBlur
                            logoConfig = newLogo
                            activeTab = StudioTab.EDITOR
                        }
                    )
                }

                StudioTab.STREAMLIT_CODE -> {
                    StreamlitCodeHub(
                        resolution = currentResolution,
                        blurConfig = blurConfig,
                        logoConfig = logoConfig
                    )
                }
            }
        }
    }
}
