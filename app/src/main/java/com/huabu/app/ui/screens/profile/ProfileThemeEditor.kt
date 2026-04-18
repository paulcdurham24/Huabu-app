package com.huabu.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*
import kotlin.math.sin
import kotlin.math.cos

// ─── Preset colour palettes ───────────────────
private data class ColorPreset(val name: String, val primary: String, val secondary: String, val accent: String, val bg: String, val card: String, val name2: String)

private val COLOR_PRESETS = listOf(
    ColorPreset("Huabu Violet",  "#8B5CF6", "#F97316", "#06B6D4", "#0D0D1A", "#1A1A2E", "#FFD700"),
    ColorPreset("Ocean Blue",    "#0EA5E9", "#38BDF8", "#7DD3FC", "#020617", "#0C1A2E", "#E0F2FE"),
    ColorPreset("Rose Gold",     "#FB7185", "#F43F5E", "#FDA4AF", "#1A0010", "#2D0018", "#FFE4E6"),
    ColorPreset("Neon Green",    "#22C55E", "#4ADE80", "#86EFAC", "#021A07", "#042B0E", "#DCFCE7"),
    ColorPreset("Sunset Fire",   "#F97316", "#EF4444", "#FBBF24", "#1A0700", "#2D0F00", "#FFF7ED"),
    ColorPreset("Cosmic Pink",   "#EC4899", "#A855F7", "#F472B6", "#1A001A", "#2D002D", "#FCE7F3"),
    ColorPreset("Ice White",     "#E2E8F0", "#CBD5E1", "#94A3B8", "#0F172A", "#1E293B", "#F8FAFC"),
    ColorPreset("Gold Rush",     "#EAB308", "#F59E0B", "#FDE68A", "#1A1200", "#2D1F00", "#FFFBEB"),
)

// ─── Helper to parse hex safely ───────────────
fun hexToColor(hex: String): Color = try {
    val cleaned = hex.trimStart('#')
    val long = ("FF$cleaned").toLong(16)
    Color(long)
} catch (e: Exception) { Color.White }

// ─── Background pattern draw ──────────────────
fun DrawScope.drawPattern(pattern: ProfileBgPattern, color: Color) {
    val c = color.copy(alpha = 0.18f)
    when (pattern) {
        ProfileBgPattern.NONE -> Unit
        ProfileBgPattern.DOTS -> {
            val step = 24f
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(c, 3f, Offset(x, y))
                    y += step
                }
                x += step
            }
        }
        ProfileBgPattern.GRID -> {
            val step = 32f
            var x = 0f
            while (x < size.width) { drawLine(c, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
            var y = 0f
            while (y < size.height) { drawLine(c, Offset(0f, y), Offset(size.width, y), 1f); y += step }
        }
        ProfileBgPattern.STARS -> {
            val positions = listOf(0.1f to 0.15f, 0.3f to 0.4f, 0.6f to 0.1f, 0.8f to 0.55f, 0.5f to 0.7f, 0.2f to 0.8f, 0.9f to 0.3f)
            positions.forEach { (fx, fy) ->
                val cx = size.width * fx; val cy = size.height * fy
                for (i in 0 until 5) {
                    val a = Math.toRadians((i * 72 - 90).toDouble())
                    val b = Math.toRadians((i * 72 - 90 + 36).toDouble())
                    drawLine(c, Offset(cx + 8f * cos(a).toFloat(), cy + 8f * sin(a).toFloat()), Offset(cx + 3f * cos(b).toFloat(), cy + 3f * sin(b).toFloat()), 1.5f)
                }
            }
        }
        ProfileBgPattern.WAVES -> {
            val step = 40f; var y = step
            while (y < size.height) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, y)
                    var x = 0f
                    while (x < size.width) { quadraticTo(x + step / 2, y - 12f, x + step, y); x += step }
                }
                drawPath(path, c, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                y += step
            }
        }
        ProfileBgPattern.DIAMONDS -> {
            val s = 28f; var row = 0
            while (row * s < size.height) {
                var col = 0
                while (col * s < size.width) {
                    val cx = col * s + if (row % 2 == 0) 0f else s / 2
                    val cy = row * s
                    drawLine(c, Offset(cx, cy - s / 2), Offset(cx + s / 2, cy), 1f)
                    drawLine(c, Offset(cx + s / 2, cy), Offset(cx, cy + s / 2), 1f)
                    drawLine(c, Offset(cx, cy + s / 2), Offset(cx - s / 2, cy), 1f)
                    drawLine(c, Offset(cx - s / 2, cy), Offset(cx, cy - s / 2), 1f)
                    col++
                }
                row++
            }
        }
        ProfileBgPattern.HEARTS -> {
            listOf(0.2f to 0.2f, 0.6f to 0.1f, 0.8f to 0.5f, 0.3f to 0.7f, 0.7f to 0.8f).forEach { (fx, fy) ->
                drawCircle(c, 10f, Offset(size.width * fx - 6f, size.height * fy))
                drawCircle(c, 10f, Offset(size.width * fx + 6f, size.height * fy))
            }
        }
        ProfileBgPattern.SPARKLES -> {
            listOf(0.15f to 0.2f, 0.5f to 0.1f, 0.8f to 0.3f, 0.3f to 0.6f, 0.7f to 0.7f, 0.1f to 0.85f).forEach { (fx, fy) ->
                val cx = size.width * fx; val cy = size.height * fy
                drawLine(c, Offset(cx - 10f, cy), Offset(cx + 10f, cy), 1.5f)
                drawLine(c, Offset(cx, cy - 10f), Offset(cx, cy + 10f), 1.5f)
                drawLine(c, Offset(cx - 7f, cy - 7f), Offset(cx + 7f, cy + 7f), 1.5f)
                drawLine(c, Offset(cx + 7f, cy - 7f), Offset(cx - 7f, cy + 7f), 1.5f)
            }
        }
    }
}

// ─── Card corner radius from style ────────────
fun cardRadius(style: ProfileCardStyle): Dp = when (style) {
    ProfileCardStyle.ROUNDED  -> 16.dp
    ProfileCardStyle.SHARP    -> 4.dp
    ProfileCardStyle.PILL     -> 32.dp
    ProfileCardStyle.GLASS    -> 20.dp
}

// ─── Font family from style ───────────────────
fun profileFontFamily(style: ProfileFontStyle): FontFamily = when (style) {
    ProfileFontStyle.DEFAULT        -> FontFamily.Default
    ProfileFontStyle.BOLD_SERIF     -> FontFamily.Serif
    ProfileFontStyle.MONO           -> FontFamily.Monospace
    ProfileFontStyle.ROUNDED        -> FontFamily.SansSerif
    ProfileFontStyle.ELEGANT_ITALIC -> FontFamily.Cursive
}

// ─────────────────────────────────────────────────────────
// ProfileThemeEditor — full screen editor with live preview
// ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileThemeEditor(
    initialTheme: ProfileTheme,
    displayName: String,
    username: String,
    onSave: (ProfileTheme) -> Unit,
    onBack: () -> Unit
) {
    var theme by remember { mutableStateOf(initialTheme) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("★ Theme Builder ★", color = HuabuGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = HuabuSilver)
                    }
                },
                actions = {
                    Button(
                        onClick = { onSave(theme) },
                        colors = ButtonDefaults.buttonColors(containerColor = hexToColor(theme.primaryColor)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuDarkBg)
            )
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Live preview
            item { ThemePreviewCard(theme = theme, displayName = displayName, username = username) }

            // Colour presets
            item { EditorSection("🎨 Colour Presets") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(COLOR_PRESETS) { preset ->
                        ColorPresetChip(preset = preset, selected = theme.primaryColor == preset.primary) {
                            theme = theme.copy(
                                primaryColor   = preset.primary,
                                secondaryColor = preset.secondary,
                                accentColor    = preset.accent,
                                backgroundColor = preset.bg,
                                cardColor      = preset.card,
                                nameColor      = preset.name2
                            )
                        }
                    }
                }
            }

            // Custom colour pickers
            item { EditorSection("🖌️ Custom Colours") }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorPickerRow("Primary", theme.primaryColor, VIVID_COLORS) { theme = theme.copy(primaryColor = it) }
                    ColorPickerRow("Secondary", theme.secondaryColor, WARM_COLORS) { theme = theme.copy(secondaryColor = it) }
                    ColorPickerRow("Accent", theme.accentColor, COOL_COLORS) { theme = theme.copy(accentColor = it) }
                    ColorPickerRow("Name colour", theme.nameColor, GOLD_COLORS) { theme = theme.copy(nameColor = it) }
                    ColorPickerRow("Background", theme.backgroundColor, DARK_COLORS) { theme = theme.copy(backgroundColor = it) }
                    ColorPickerRow("Card colour", theme.cardColor, DARK_COLORS) { theme = theme.copy(cardColor = it) }
                }
            }

            // Background pattern
            item { EditorSection("✨ Background Pattern") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProfileBgPattern.entries) { pattern ->
                        StyleChip(
                            label = pattern.label(),
                            selected = theme.bgPattern == pattern
                        ) { theme = theme.copy(bgPattern = pattern) }
                    }
                }
            }

            // Card style
            item { EditorSection("🃏 Card Style") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProfileCardStyle.entries) { style ->
                        StyleChip(
                            label = style.label(),
                            selected = theme.cardStyle == style
                        ) { theme = theme.copy(cardStyle = style) }
                    }
                }
            }

            // Profile border
            item { EditorSection("💫 Profile Picture Border") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProfileBorderStyle.entries) { style ->
                        BorderStyleChip(
                            style = style,
                            theme = theme,
                            selected = theme.borderStyle == style
                        ) { theme = theme.copy(borderStyle = style) }
                    }
                }
            }

            // Font style
            item { EditorSection("🔤 Name Font Style") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProfileFontStyle.entries) { font ->
                        FontStyleChip(
                            font = font,
                            nameColor = hexToColor(theme.nameColor),
                            selected = theme.fontStyle == font
                        ) { theme = theme.copy(fontStyle = font) }
                    }
                }
            }

            // Opacity sliders
            item { EditorSection("🌫️ Transparency") }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OpacitySlider("Card opacity", theme.cardOpacity) { theme = theme.copy(cardOpacity = it) }
                    OpacitySlider("Banner opacity", theme.bannerOpacity) { theme = theme.copy(bannerOpacity = it) }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─── Live preview mini card ───────────────────

@Composable
private fun ThemePreviewCard(theme: ProfileTheme, displayName: String, username: String) {
    val bg = hexToColor(theme.backgroundColor)
    val primary = hexToColor(theme.primaryColor)
    val secondary = hexToColor(theme.secondaryColor)
    val nameCol = hexToColor(theme.nameColor)
    val usernameCol = hexToColor(theme.usernameColor)
    val cardCol = hexToColor(theme.cardColor)
    val radius = cardRadius(theme.cardStyle)
    val ff = profileFontFamily(theme.fontStyle)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            Text(
                "Live Preview",
                modifier = Modifier.padding(12.dp, 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = HuabuSilver
            )
            HorizontalDivider(color = HuabuDivider)

            // Mini banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        if (theme.useGradientBg)
                            Brush.linearGradient(listOf(bg, hexToColor(theme.gradientColor2)))
                        else
                            Brush.linearGradient(listOf(bg, bg))
                    )
                    .drawBehind { drawPattern(theme.bgPattern, primary) },
                contentAlignment = Alignment.Center
            ) {
                // Avatar preview
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .then(avatarBorderModifier(theme.borderStyle, primary, secondary))
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(primary.copy(0.6f), cardCol))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        displayName.firstOrNull()?.uppercase() ?: "?",
                        color = nameCol,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        fontFamily = ff
                    )
                }
            }

            // Info row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardCol.copy(alpha = theme.cardOpacity))
                    .padding(16.dp, 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = displayName,
                        color = nameCol,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        fontFamily = ff
                    )
                    Text(
                        text = "@$username",
                        color = usernameCol,
                        fontSize = 13.sp,
                        fontFamily = ff
                    )
                    Spacer(Modifier.height(6.dp))
                    // Mini widget card preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(radius))
                            .background(primary.copy(alpha = 0.18f))
                            .border(1.dp, primary.copy(0.4f), RoundedCornerShape(radius))
                            .padding(8.dp)
                    ) {
                        Text("Widget card preview", color = hexToColor(theme.textColor), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ─── Avatar border modifier ───────────────────

fun avatarBorderModifier(style: ProfileBorderStyle, primary: Color, secondary: Color): Modifier = when (style) {
    ProfileBorderStyle.NONE    -> Modifier
    ProfileBorderStyle.SOLID   -> Modifier.border(3.dp, primary, CircleShape)
    ProfileBorderStyle.RAINBOW -> Modifier.border(3.dp, Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)), CircleShape)
    ProfileBorderStyle.GOLD_GLOW -> Modifier.border(4.dp, Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))), CircleShape)
    ProfileBorderStyle.NEON    -> Modifier.border(3.dp, primary, CircleShape)
    ProfileBorderStyle.DOUBLE  -> Modifier.border(5.dp, primary, CircleShape).padding(3.dp).border(2.dp, secondary, CircleShape)
}

// ─── Colour picker row ───────────────────────

@Composable
private fun ColorPickerRow(label: String, current: String, palette: List<String>, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = HuabuSilver)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(palette) { hex ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(hexToColor(hex))
                        .then(if (current == hex) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                        .clickable { onPick(hex) }
                )
            }
        }
    }
}

// ─── Style chips ─────────────────────────────

@Composable
private fun StyleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) HuabuViolet else HuabuCardBg2,
        border = if (selected) null else ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else HuabuSilver
        )
    }
}

@Composable
private fun ColorPresetChip(preset: ColorPreset, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) HuabuSurface else HuabuCardBg2)
            .border(if (selected) 2.dp else 1.dp, if (selected) HuabuGold else HuabuDivider, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            listOf(preset.primary, preset.secondary, preset.accent).forEach {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(hexToColor(it)))
            }
        }
        Text(preset.name, style = MaterialTheme.typography.labelSmall, color = if (selected) HuabuGold else HuabuSilver)
    }
}

@Composable
private fun FontStyleChip(font: ProfileFontStyle, nameColor: Color, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) HuabuViolet else HuabuCardBg2,
        modifier = Modifier
            .width(90.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aa",
                fontFamily = profileFontFamily(font),
                fontWeight = if (font == ProfileFontStyle.BOLD_SERIF) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (font == ProfileFontStyle.ELEGANT_ITALIC) FontStyle.Italic else FontStyle.Normal,
                fontSize = 20.sp,
                color = if (selected) Color.White else nameColor
            )
            Text(
                text = font.label(),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else HuabuSilver
            )
        }
    }
}

@Composable
private fun BorderStyleChip(style: ProfileBorderStyle, theme: ProfileTheme, selected: Boolean, onClick: () -> Unit) {
    val primary = hexToColor(theme.primaryColor)
    val secondary = hexToColor(theme.secondaryColor)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) HuabuSurface else HuabuCardBg2)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .then(avatarBorderModifier(style, primary, secondary))
                .clip(CircleShape)
                .background(HuabuDeepPurple),
            contentAlignment = Alignment.Center
        ) {
            Text("A", color = hexToColor(theme.nameColor), fontWeight = FontWeight.Bold)
        }
        Text(style.label(), style = MaterialTheme.typography.labelSmall, color = if (selected) HuabuGold else HuabuSilver)
    }
}

// ─── Opacity slider ──────────────────────────

@Composable
private fun OpacitySlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.labelMedium, color = HuabuSilver, modifier = Modifier.weight(1f))
            Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = HuabuGold)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0.3f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = HuabuViolet,
                activeTrackColor = HuabuViolet,
                inactiveTrackColor = HuabuDivider
            )
        )
    }
}

// ─── Section header ──────────────────────────

@Composable
private fun EditorSection(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = HuabuGold
    )
}

// ─── Label extensions ────────────────────────

private fun ProfileBgPattern.label() = when (this) {
    ProfileBgPattern.NONE      -> "None"
    ProfileBgPattern.STARS     -> "Stars"
    ProfileBgPattern.DOTS      -> "Dots"
    ProfileBgPattern.GRID      -> "Grid"
    ProfileBgPattern.WAVES     -> "Waves"
    ProfileBgPattern.DIAMONDS  -> "Diamonds"
    ProfileBgPattern.HEARTS    -> "Hearts"
    ProfileBgPattern.SPARKLES  -> "Sparkles"
}

private fun ProfileCardStyle.label() = when (this) {
    ProfileCardStyle.ROUNDED -> "Rounded"
    ProfileCardStyle.SHARP   -> "Sharp"
    ProfileCardStyle.PILL    -> "Pill"
    ProfileCardStyle.GLASS   -> "Glass"
}

private fun ProfileBorderStyle.label() = when (this) {
    ProfileBorderStyle.NONE      -> "None"
    ProfileBorderStyle.SOLID     -> "Solid"
    ProfileBorderStyle.RAINBOW   -> "Rainbow"
    ProfileBorderStyle.GOLD_GLOW -> "Gold"
    ProfileBorderStyle.NEON      -> "Neon"
    ProfileBorderStyle.DOUBLE    -> "Double"
}

private fun ProfileFontStyle.label() = when (this) {
    ProfileFontStyle.DEFAULT        -> "Default"
    ProfileFontStyle.BOLD_SERIF     -> "Serif"
    ProfileFontStyle.MONO           -> "Mono"
    ProfileFontStyle.ROUNDED        -> "Round"
    ProfileFontStyle.ELEGANT_ITALIC -> "Italic"
}

// ─── Colour palettes ─────────────────────────

private val VIVID_COLORS = listOf("#8B5CF6","#A855F7","#EC4899","#EF4444","#F97316","#EAB308","#22C55E","#06B6D4","#3B82F6","#6366F1")
private val WARM_COLORS  = listOf("#F97316","#EF4444","#FB7185","#F43F5E","#E879F9","#C026D3","#FBBF24","#F59E0B","#84CC16","#A3E635")
private val COOL_COLORS  = listOf("#06B6D4","#0EA5E9","#3B82F6","#6366F1","#8B5CF6","#A78BFA","#38BDF8","#7DD3FC","#22D3EE","#67E8F9")
private val GOLD_COLORS  = listOf("#FFD700","#FFA500","#F59E0B","#EAB308","#FBBF24","#FDE68A","#FFFFFF","#E2E8F0","#F8FAFC","#CBD5E1")
private val DARK_COLORS  = listOf("#0D0D1A","#020617","#0C1A2E","#1A0010","#1A0700","#042B0E","#0F172A","#1E293B","#1A001A","#1A1200")
