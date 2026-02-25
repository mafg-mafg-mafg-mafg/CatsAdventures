package com.mafg.mafg.mafg.mafg.catsadventures.presentation.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pendulum")
    val angleDegrees by infiniteTransition.animateFloat(
        initialValue = -45f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "angle"
    )

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val anchor = Offset(canvasWidth / 2, 50f)
        val length = canvasHeight * 0.6f

        val angleRadians = (angleDegrees + 90f) * (PI.toFloat() / 180f)

        val endPoint = Offset(
            x = anchor.x + length * cos(angleRadians),
            y = anchor.y + length * sin(angleRadians)
        )

        // Draw the string
        drawLine(
            color = Color.Black,
            start = anchor,
            end = endPoint,
            strokeWidth = 4f
        )

        // Draw the bob
        drawCircle(
            color = Color.Red,
            center = endPoint,
            radius = 40f
        )
    }
}