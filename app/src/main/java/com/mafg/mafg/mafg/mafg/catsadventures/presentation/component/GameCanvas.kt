package com.mafg.mafg.mafg.mafg.catsadventures.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(modifier: Modifier = Modifier) {
    // Estados del juego
    var score by remember { mutableIntStateOf(0) }
    var speedMultiplier by remember { mutableStateOf(1f) }
    var time by remember { mutableStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Estado del cuadrado perseguidor
    var squarePos by remember { mutableStateOf<Offset?>(null) }
    val squareSize = 60f

    val animatedSpeed by animateFloatAsState(
        targetValue = speedMultiplier,
        animationSpec = tween(durationMillis = 300),
        label = "speed"
    )

    // Posición actual de la bola (para que el cuadrado la persiga)
    var currentBallPos by remember { mutableStateOf(Offset.Zero) }

    // Bucle de animación y lógica de juego
    LaunchedEffect(Unit) {
        var lastFrameTime = System.nanoTime()
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
                
                time += deltaTime * animatedSpeed

                if (canvasSize.width > 0) {
                    val cw = canvasSize.width.toFloat()
                    val ch = canvasSize.height.toFloat()

                    // 1. Recalcular posición del péndulo para la lógica
                    val angleDegrees = 45f * sin(time * 3f)
                    val vOffset = 200f + 200f * sin(time * 2f)
                    val anchorY = 20f + vOffset
                    val anchor = Offset(cw / 2, anchorY)
                    val ropeLength = ch * 0.3f
                    val angleRadians = (angleDegrees + 90f) * (PI.toFloat() / 180f)
                    
                    currentBallPos = Offset(
                        x = anchor.x + ropeLength * cos(angleRadians),
                        y = anchor.y + ropeLength * sin(angleRadians)
                    )

                    // 2. Lógica del cuadrado perseguidor
                    if (squarePos == null) {
                        squarePos = Offset(cw / 2, ch - 200f)
                    }

                    squarePos?.let { pos ->
                        val direction = currentBallPos - pos
                        val distance = direction.getDistance()
                        val moveSpeed = 250f * deltaTime * animatedSpeed

                        if (distance < 50f) { // Colisión
                            score++
                            squarePos = Offset(cw / 2, ch - 100f) // Reiniciar posición
                        } else {
                            val velocity = direction / distance * moveSpeed
                            squarePos = pos + velocity
                        }
                    }
                }
            }
        }
    }

    val trailPositions = remember { mutableStateListOf<Offset>() }

    Box(modifier = modifier.fillMaxSize()) {
        // Contador centrado arriba
        Text(
            text = "PUNTOS: $score",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.DarkGray,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            speedMultiplier = 5f
                            tryAwaitRelease()
                            speedMultiplier = 1f
                        }
                    )
                }
        ) {
            val cw = size.width
            val ch = size.height

            // --- DIBUJO PÉNDULO ---
            val angleDegrees = 45f * sin(time * 3f)
            val vOffset = 200f + 200f * sin(time * 2f)
            val anchor = Offset(cw / 2, 20f + vOffset)
            val ropeLength = ch * 0.3f
            val angleRadians = (angleDegrees + 90f) * (PI.toFloat() / 180f)
            val endPoint = Offset(
                x = anchor.x + ropeLength * cos(angleRadians),
                y = anchor.y + ropeLength * sin(angleRadians)
            )

            trailPositions.add(endPoint)
            if (trailPositions.size > 15) trailPositions.removeAt(0)

            trailPositions.forEachIndexed { index, position ->
                val alpha = (index.toFloat() / trailPositions.size) * 0.3f
                drawCircle(Color.Red.copy(alpha = alpha), center = position, radius = 35f * (index.toFloat() / trailPositions.size))
            }

            drawLine(Color.Black, start = anchor, end = endPoint, strokeWidth = 4f)
            
            val ballColor = if (animatedSpeed > 2f) Color.Magenta else Color.Red
            drawCircle(color = ballColor, center = endPoint, radius = 40f)

            // --- DIBUJO CUADRADO PERSEGUIDOR ---
            squarePos?.let { pos ->
                drawRect(
                    color = Color(0xFF388E3C), // Verde oscuro
                    topLeft = Offset(pos.x - squareSize / 2, pos.y - squareSize / 2),
                    size = Size(squareSize, squareSize)
                )
            }
        }
    }
}