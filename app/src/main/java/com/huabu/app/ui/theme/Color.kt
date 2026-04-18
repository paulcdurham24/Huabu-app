package com.huabu.app.ui.theme

import androidx.compose.ui.graphics.Color

// Huabu palette — extracted from official icon artwork
// Background: violet #7B3FF2 | Gradient: coral #E8614A → violet #7B3FF2
// Accent blue #29B6F6 | Lime #C6F135 | Deep purple #5C2EBF | Cyan #00CFFF

// Primary brand colours (from icon)
val HuabuViolet = Color(0xFF7B3FF2)       // icon background / primary
val HuabuCoral = Color(0xFFE8614A)        // icon gradient start (top-left)
val HuabuDeepPurple = Color(0xFF5C2EBF)   // icon dark purple panels
val HuabuSkyBlue = Color(0xFF29B6F6)      // icon "H" panel highlight
val HuabuLime = Color(0xFFC6F135)         // icon "H" panel accent
val HuabuCyan = Color(0xFF00CFFF)         // icon camera lens / highlight

// Alias names kept for backwards compatibility with screens
val HuabuHotPink = HuabuCoral             // coral replaces hot pink as primary CTA
val HuabuElectricBlue = HuabuSkyBlue      // sky blue replaces electric blue
val HuabuNeonGreen = HuabuLime            // lime replaces neon green
val HuabuGold = Color(0xFFFFD700)         // kept — used for star ratings / top friends
val HuabuAccentCyan = HuabuCyan
val HuabuAccentOrange = HuabuCoral

// Surfaces — dark mode tuned to icon's deep violet atmosphere
val HuabuDarkBg = Color(0xFF0E0818)       // near-black with violet tint
val HuabuCardBg = Color(0xFF1C1336)       // card surface — deep violet-black
val HuabuCardBg2 = Color(0xFF231744)      // secondary card / elevated
val HuabuSurface = Color(0xFF2D1B69)      // surface layer — rich purple
val HuabuOnSurface = Color(0xFFF2EEFF)    // text on dark surfaces
val HuabuSilver = Color(0xFFB0A8C8)       // muted text — purple-grey
val HuabuAccentPink = Color(0xFFE040FB)   // retained for tags / badges
val HuabuError = Color(0xFFCF6679)
val HuabuDivider = Color(0xFF2E1F55)

// Gradient — matches icon's coral → violet sweep
val GradientStart = HuabuCoral            // #E8614A
val GradientMid = HuabuViolet             // #7B3FF2
val GradientEnd = HuabuDeepPurple         // #5C2EBF
