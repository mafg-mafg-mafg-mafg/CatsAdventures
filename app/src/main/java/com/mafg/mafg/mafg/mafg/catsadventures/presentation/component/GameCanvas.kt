package com.mafg.mafg.mafg.mafg.catsadventures.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(modifier: Modifier = Modifier) {
    var score by remember { mutableIntStateOf(10) }
    var timeLeft by remember { mutableIntStateOf(50) } // 50 segundos
    var time by remember { mutableStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val pawExtension = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    var isRainbowActive by remember { mutableStateOf(false) }
    
    var targetPos by remember { mutableStateOf(Offset.Zero) }
    var currentBallPos by remember { mutableStateOf(Offset.Zero) }

    // Temporizador que baja cada segundo
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    LaunchedEffect(Unit) {
        var lastFrameTime = System.nanoTime()
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
                time += deltaTime
            }
        }
    }

    val trailPositions = remember { mutableStateListOf<Offset>() }

    val ballColor = if (isRainbowActive) {
        val hue = (time * 360f) % 360f
        Color.hsv(hue, 0.7f, 0.9f)
    } else {
        Color(0xFFAEC6CF) // Azul pastel
    }

    // Definición del degradado azul (de oscuro a claro)
    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D47A1), // Azul oscuro (Blue 900)
            Color(0xFFE3F2FD)  // Azul claro (Blue 50)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(blueGradient)
    ) {
        // Temporizador en la esquina superior izquierda
        val minutes = timeLeft / 60
        val seconds = timeLeft % 60
        val timerText = "%02d:%02d".format(minutes, seconds)
        
        Text(
            text = timerText,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White, // Siempre blanco
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 20.dp, start = 20.dp)
        )

        // Contador de colisiones en la esquina superior derecha
        Text(
            text = "$score",
            style = MaterialTheme.typography.displayLarge,
            color = Color.White, // Siempre blanco
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!pawExtension.isRunning && score > 0 && timeLeft > 0) {
                                targetPos = currentBallPos
                                scope.launch {
                                    pawExtension.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
                                    )
                                    
                                    val ch = canvasSize.height.toFloat()
                                    val cw = canvasSize.width.toFloat()
                                    val startPos = Offset(cw / 2, ch)
                                    val tipPos = startPos + (targetPos - startPos) * 1f
                                    
                                    val distance = (currentBallPos - tipPos).getDistance()
                                    if (distance < 100f) { 
                                        score--
                                        isRainbowActive = true
                                        launch {
                                            delay(3000)
                                            isRainbowActive = false
                                        }
                                    }

                                    pawExtension.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                                    )
                                }
                            }
                        }
                    )
                }
        ) {
            val cw = size.width
            val ch = size.height

            val angleDegrees = 45f * sin(time * 3f)
            val vOffset = 200f + 200f * sin(time * 2f)
            val anchor = Offset(cw / 2, 20f + vOffset)
            val ropeLength = ch * 0.3f
            val angleRadians = (angleDegrees + 90f) * (PI.toFloat() / 180f)
            currentBallPos = Offset(
                x = anchor.x + ropeLength * cos(angleRadians),
                y = anchor.y + ropeLength * sin(angleRadians)
            )

            val ballRadius = 60f

            trailPositions.add(currentBallPos)
            if (trailPositions.size > 15) trailPositions.removeAt(0)
            trailPositions.forEachIndexed { index, position ->
                val alpha = (index.toFloat() / trailPositions.size) * 0.3f
                drawCircle(ballColor.copy(alpha = alpha), center = position, radius = (ballRadius - 5f) * (index.toFloat() / trailPositions.size))
            }

            drawCircle(color = ballColor, center = currentBallPos, radius = ballRadius)

            val startPos = Offset(cw / 2, ch)
            val currentTipPos = startPos + (targetPos - startPos) * pawExtension.value

            val pawWidth = 100f
            drawLine(
                color = Color.Black,
                start = startPos,
                end = currentTipPos,
                strokeWidth = pawWidth,
                cap = StrokeCap.Round
            )
            
            val bigTipRadius = pawWidth * 0.9f
            drawCircle(color = Color.Black, center = currentTipPos, radius = bigTipRadius)

            if (pawExtension.value > 0.1f) {
                val pawDirection = targetPos - startPos
                val dist = pawDirection.getDistance().coerceAtLeast(1f)
                val unitDir = pawDirection / dist
                val perpDir = Offset(-unitDir.y, unitDir.x)

                for (i in -1..1) {
                    val clawStart = currentTipPos + (perpDir * (i * 30f)) + (unitDir * 20f)
                    val clawEnd = clawStart + (unitDir * 35f)
                    drawLine(Color.White, start = clawStart, end = clawEnd, strokeWidth = 8f, cap = StrokeCap.Round)
                }
            }
        }
    }
}