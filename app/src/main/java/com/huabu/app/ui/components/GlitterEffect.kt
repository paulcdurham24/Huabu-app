package com.huabu.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Sparkle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val angle: Float,
    val speed: Float,
    val alpha: Float
)

@Composable
fun GlitterCanvas(
    modifier: Modifier = Modifier,
    sparkleCount: Int = 20,
    colors: List<Color> = listOf(
        Color(0xFF7B3FF2),
        Color(0xFFE8614A),
        Color(0xFF00CFFF),
        Color(0xFFC6F135),
        Color(0xFF29B6F6),
        Color(0xFFFFD700)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glitter")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val sparkles = remember {
        List(sparkleCount) {
            Sparkle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 1f,
                color = colors.random(),
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 0.3f + 0.1f,
                alpha = Random.nextFloat() * 0.8f + 0.2f
            )
        }
    }

    Canvas(modifier = modifier) {
        sparkles.forEach { sparkle ->
            val animatedAlpha = ((sin((time + sparkle.angle) * Math.PI / 180f) + 1f) / 2f).toFloat()
            drawCircle(
                color = sparkle.color.copy(alpha = animatedAlpha * sparkle.alpha),
                radius = sparkle.radius,
                center = Offset(
                    x = sparkle.x * size.width,
                    y = sparkle.y * size.height
                )
            )
            drawStar(
                center = Offset(sparkle.x * size.width, sparkle.y * size.height),
                color = sparkle.color.copy(alpha = animatedAlpha * sparkle.alpha * 0.5f),
                outerRadius = sparkle.radius * 2f,
                innerRadius = sparkle.radius,
                rotation = time * sparkle.speed + sparkle.angle
            )
        }
    }
}

private fun DrawScope.drawStar(
    center: Offset,
    color: Color,
    outerRadius: Float,
    innerRadius: Float,
    rotation: Float,
    points: Int = 4
) {
    val path = androidx.compose.ui.graphics.Path()
    val angleStep = 360f / (points * 2)
    var first = true
    for (i in 0 until points * 2) {
        val angle = (rotation + i * angleStep) * Math.PI / 180f
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (first) { path.moveTo(x, y); first = false } else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}
