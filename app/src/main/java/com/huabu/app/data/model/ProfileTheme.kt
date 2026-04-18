package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProfileFontStyle {
    DEFAULT, BOLD_SERIF, MONO, ROUNDED, ELEGANT_ITALIC
}

enum class ProfileCardStyle {
    ROUNDED, SHARP, PILL, GLASS
}

enum class ProfileBgPattern {
    NONE, STARS, DOTS, GRID, WAVES, DIAMONDS, HEARTS, SPARKLES
}

enum class ProfileBorderStyle {
    NONE, SOLID, RAINBOW, GOLD_GLOW, NEON, DOUBLE
}

enum class ProfileLayoutStyle {
    CLASSIC, COMPACT, MAGAZINE, MINIMAL
}

@Entity(tableName = "profile_themes")
data class ProfileTheme(
    @PrimaryKey val userId: String,

    // Colours (hex strings)
    val primaryColor: String = "#8B5CF6",
    val secondaryColor: String = "#F97316",
    val accentColor: String = "#06B6D4",
    val backgroundColor: String = "#0D0D1A",
    val cardColor: String = "#1A1A2E",
    val textColor: String = "#FFFFFF",
    val nameColor: String = "#FFD700",
    val usernameColor: String = "#F97316",

    // Style choices
    val fontStyle: ProfileFontStyle = ProfileFontStyle.DEFAULT,
    val cardStyle: ProfileCardStyle = ProfileCardStyle.ROUNDED,
    val bgPattern: ProfileBgPattern = ProfileBgPattern.STARS,
    val borderStyle: ProfileBorderStyle = ProfileBorderStyle.RAINBOW,
    val layoutStyle: ProfileLayoutStyle = ProfileLayoutStyle.CLASSIC,

    // Extras
    val bannerOpacity: Float = 0.85f,
    val cardOpacity: Float = 0.90f,
    val useGradientBg: Boolean = true,
    val gradientColor2: String = "#1A0533"
)
