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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameEvent
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvas(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
) {
    val state by viewModel.gameState.collectAsState()
    
    var time by remember { mutableStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val pawExtension = remember { Animatable(0f) }
    val ballAlpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    var targetPos by remember { mutableStateOf(Offset.Zero) }
    var currentBallPos by remember { mutableStateOf(Offset.Zero) }
    
    var isBallCaught by remember { mutableStateOf(false) }

    LaunchedEffect(state.timeLeft) {
        if (state.timeLeft > 0) {
            delay(1000)
            viewModel.onEvent(GameEvent.DecrementTime)
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

    val ballColor = if (state.isRainbowActive) {
        val hue = (time * 360f) % 360f
        Color.hsv(hue, 0.7f, 0.9f)
    } else {
        Color(0xFFFF8A80) // Rojo claro mate
    }

    val matteLightBlue = Color(0xFFB3E5FC) // Azul claro mate

    Box(modifier = modifier.fillMaxSize().background(matteLightBlue)) {
        Text(
            text = "%02d:%02d".format(state.timeLeft / 60, state.timeLeft % 60),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp)
        )

        Text(
            text = "${state.score}",
            style = MaterialTheme.typography.displayLarge,
            color = Color.Black,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!pawExtension.isRunning && state.score > 0 && state.timeLeft > 0 && ballAlpha.value > 0.9f) {
                                targetPos = currentBallPos
                                scope.launch {
                                    // Extender la pata
                                    pawExtension.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
                                    
                                    val startPos = Offset(canvasSize.width / 2f, canvasSize.height.toFloat())
                                    val tipPos = startPos + (targetPos - startPos) * 1f
                                    
                                    // Comprobar si atrapó la pelota
                                    if ((currentBallPos - tipPos).getDistance() < 120f) {
                                        isBallCaught = true
                                        viewModel.onEvent(GameEvent.DecrementScore)
                                    }
                                    
                                    // Retraer la pata (llevándose la pelota si fue atrapada)
                                    pawExtension.animateTo(0f, tween(400, easing = LinearOutSlowInEasing))
                                    
                                    if (isBallCaught) {
                                        // Efecto de desaparecer la pelota al llegar abajo
                                        ballAlpha.animateTo(0f, tween(150))
                                        isBallCaught = false
                                        delay(300) // Tiempo "desaparecida"
                                        // Reaparecer la pelota en el péndulo
                                        ballAlpha.animateTo(1f, tween(300))
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            val cw = size.width
            val ch = size.height

            // --- BOSQUE DE FONDO ---
            drawForest(cw, ch)

            // --- PÉNDULO ---
            val angleDegrees = 45f * sin(time * 3f)
            val vOffset = 200f + 200f * sin(time * 2f)
            val anchor = Offset(cw / 2, 20f + vOffset)
            val ropeLength = ch * 0.3f
            val angleRadians = (angleDegrees + 90f) * (PI.toFloat() / 180f)
            
            val pendulumBallPos = Offset(
                x = anchor.x + ropeLength * cos(angleRadians),
                y = anchor.y + ropeLength * sin(angleRadians)
            )

            // Si la pelota no está atrapada, sigue al péndulo
            if (!isBallCaught) {
                currentBallPos = pendulumBallPos
            }

            val ballRadius = 60f
            
            // Dibujar rastro solo si no está atrapada y es visible
            if (!isBallCaught && ballAlpha.value > 0.01f) {
                trailPositions.add(currentBallPos)
                if (trailPositions.size > 15) trailPositions.removeAt(0)
                trailPositions.forEachIndexed { index, position ->
                    val alpha = (index.toFloat() / trailPositions.size) * 0.3f * ballAlpha.value
                    drawYarnBall(center = position, radius = (ballRadius - 5f) * (index.toFloat() / trailPositions.size), color = Color.White, alpha = alpha)
                }
            } else {
                trailPositions.clear()
            }

            // --- HUECO DE LA PATA (OVALO NEGRO EN LA BASE) ---
            val holeWidth = 200f
            val holeHeight = 60f
            drawOval(
                color = Color.Black,
                topLeft = Offset((cw / 2) - (holeWidth / 2), ch - (holeHeight / 2)),
                size = Size(holeWidth, holeHeight)
            )

            // --- PATA DEL GATO REALISTA ---
            val startPos = Offset(cw / 2, ch)
            val restingPos = Offset(cw / 2, ch - 80f)
            val currentTipPos = if (pawExtension.value > 0.01f) {
                startPos + (targetPos - startPos) * pawExtension.value
            } else {
                restingPos
            }
            
            // --- LÍNEA PUNTEADA DE TRAYECTORIA ---
            // Siempre visible: apunta a targetPos si se está moviendo, o a currentBallPos para apuntar
            val lineEnd = if (pawExtension.isRunning) targetPos else currentBallPos
            
            if (ballAlpha.value > 0.9f) { // Solo mostrar si la pelota es visible (para apuntar)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = startPos,
                    end = lineEnd,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
            
            // Si la pelota está atrapada, su posición sigue a la pata
            if (isBallCaught) {
                currentBallPos = currentTipPos
            }

            // Dibujar la pelota con su transparencia actual
            if (ballAlpha.value > 0.01f) {
                drawYarnBall(
                    color = ballColor,
                    center = currentBallPos,
                    radius = ballRadius,
                    alpha = ballAlpha.value
                )
            }
            
            drawRealisticPaw(startPos, currentTipPos)
        }
    }
}

fun DrawScope.drawForest(cw: Float, ch: Float) {
    val groundHeight = 120f
    
    // Suelo/Pasto
    drawRect(
        color = Color(0xFF2E7D32),
        topLeft = Offset(0f, ch - groundHeight),
        size = Size(cw, groundHeight)
    )

    // Árboles distantes (más pequeños y oscuros)
    val randomDist = Random(123)
    for (i in 0..8) {
        val x = randomDist.nextFloat() * cw
        val scale = 0.5f + randomDist.nextFloat() * 0.5f
        drawTree(x, ch - groundHeight + 20f, scale, Color(0xFF1B5E20))
    }

    // Árboles cercanos
    val randomNear = Random(456)
    for (i in 0..4) {
        val x = (cw / 5) * i + randomNear.nextFloat() * (cw / 10)
        val scale = 0.8f + randomNear.nextFloat() * 0.4f
        drawTree(x, ch - groundHeight + 50f, scale, Color(0xFF388E3C))
    }
}

fun DrawScope.drawTree(x: Float, groundY: Float, scale: Float, leafColor: Color) {
    val trunkWidth = 15f * scale
    val trunkHeight = 30f * scale
    val leavesWidth = 60f * scale
    val leavesHeight = 80f * scale

    // Tronco
    drawRect(
        color = Color(0xFF3E2723),
        topLeft = Offset(x - trunkWidth / 2, groundY - trunkHeight),
        size = Size(trunkWidth, trunkHeight)
    )

    // Hojas (Copa triangular)
    val path = Path().apply {
        moveTo(x, groundY - trunkHeight - leavesHeight)
        lineTo(x - leavesWidth / 2, groundY - trunkHeight)
        lineTo(x + leavesWidth / 2, groundY - trunkHeight)
        close()
    }
    drawPath(path, color = leafColor)
}

fun DrawScope.drawYarnBall(center: Offset, radius: Float, color: Color, alpha: Float) {
    if (radius <= 0f) return
    
    val ballColor = color.copy(alpha = alpha)
    val strandColor = color.copy(alpha = alpha * 0.7f)
    val darkStrandColor = Color.Black.copy(alpha = alpha * 0.2f)
    val highlightColor = Color.White.copy(alpha = alpha * 0.4f)

    // Sombra proyectada ligera
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.2f * alpha), Color.Transparent),
            center = center + Offset(radius * 0.2f, radius * 0.2f),
            radius = radius * 1.2f
        ),
        center = center + Offset(radius * 0.1f, radius * 0.1f),
        radius = radius * 1.1f
    )

    // Base de la pelota con un degradado sutil para volumen
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.8f).compositeOver(Color.Black.copy(0.2f))),
            center = center - Offset(radius * 0.3f, radius * 0.3f),
            radius = radius * 1.5f
        ),
        center = center,
        radius = radius
    )

    // Muchas hebras entrelazadas aleatoriamente
    val random = Random(center.hashCode())
    for (i in 0..15) {
        val startAngle = random.nextFloat() * 360f
        val sweepAngle = 90f + random.nextFloat() * 180f
        
        // Hebras oscuras para profundidad
        drawArc(
            color = darkStrandColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.9f, center.y - radius * 0.9f),
            size = Size(radius * 1.8f, radius * 1.8f),
            style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round)
        )

        // Hebras principales del color de la pelota
        drawArc(
            color = strandColor,
            startAngle = startAngle + 5f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.85f, center.y - radius * 0.85f),
            size = Size(radius * 1.7f, radius * 1.7f),
            style = Stroke(width = radius * 0.12f, cap = StrokeCap.Round)
        )
    }

    // Hebras de brillo en la parte superior
    for (i in 0..5) {
        val startAngle = 200f + random.nextFloat() * 100f
        drawArc(
            color = highlightColor,
            startAngle = startAngle,
            sweepAngle = 40f + random.nextFloat() * 60f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.7f, center.y - radius * 0.7f),
            size = Size(radius * 1.4f, radius * 1.4f),
            style = Stroke(width = radius * 0.06f, cap = StrokeCap.Round)
        )
    }
}

// Extensión necesaria para simular compositeOver de forma sencilla
fun Color.compositeOver(background: Color): Color {
    return Color(
        red = this.red * this.alpha + background.red * background.alpha * (1 - this.alpha),
        green = this.green * this.alpha + background.green * background.alpha * (1 - this.alpha),
        blue = this.blue * this.alpha + background.blue * background.alpha * (1 - this.alpha),
        alpha = this.alpha + background.alpha * (1 - this.alpha)
    )
}

fun DrawScope.drawRealisticPaw(start: Offset, end: Offset) {
    val vector = end - start
    val angle = if (vector.getDistance() < 1f) 0f else {
        Math.toDegrees(atan2(vector.y.toDouble(), vector.x.toDouble())).toFloat() + 90f
    }
    val length = vector.getDistance()
    
    val pawColor = Color(0xFFF5F5F5)
    val shadowColor = Color(0xFFE0E0E0)
    val padColor = Color(0xFFFFB7C5)
    val furTextureColor = Color(0xFFBDBDBD).copy(alpha = 0.3f)

    translate(start.x, start.y) {
        rotate(angle, pivot = Offset.Zero) {
            val armWidth = 120f
            val armPath = Path().apply {
                moveTo(-armWidth / 2, 0f)
                lineTo(-armWidth / 2.5f, -length)
                quadraticTo(0f, -length - 40f, armWidth / 2.5f, -length)
                lineTo(armWidth / 2, 0f)
                close()
            }
            
            drawPath(
                path = armPath,
                brush = Brush.verticalGradient(
                    0f to Color.Black,
                    0.8f to pawColor,
                    1f to Color.White
                )
            )

            val headWidth = 160f
            val headHeight = 140f
            val headOffset = -length
            
            drawOval(
                color = pawColor,
                topLeft = Offset(-headWidth / 2, headOffset - headHeight / 2),
                size = Size(headWidth, headHeight)
            )

            val toeRadius = 35f
            val toeOffsets = listOf(
                Offset(-60f, headOffset - 20f),
                Offset(-25f, headOffset - 50f),
                Offset(25f, headOffset - 50f),
                Offset(60f, headOffset - 20f)
            )

            toeOffsets.forEach { pos ->
                drawCircle(color = shadowColor, center = pos + Offset(0f, 5f), radius = toeRadius)
                drawCircle(color = pawColor, center = pos, radius = toeRadius)
                drawCircle(color = padColor, center = pos + Offset(0f, -5f), radius = toeRadius * 0.6f)
                
                val clawPath = Path().apply {
                    moveTo(pos.x - 4f, pos.y - toeRadius + 5f)
                    lineTo(pos.x, pos.y - toeRadius - 25f)
                    lineTo(pos.x + 4f, pos.y - toeRadius + 5f)
                    close()
                }
                drawPath(clawPath, color = Color(0xFFEEEEEE))
            }

            val mainPadPath = Path().apply {
                val cx = 0f
                val cy = headOffset + 20f
                moveTo(cx, cy + 10f)
                cubicTo(cx - 50f, cy - 10f, cx - 40f, cy + 50f, cx, cy + 50f)
                cubicTo(cx + 40f, cy + 50f, cx + 50f, cy - 10f, cx, cy + 10f)
                close()
            }
            drawPath(mainPadPath, color = padColor)

            for (i in 0..10) {
                val x = Random.nextInt(-70, 71).toFloat()
                val y = Random.nextInt((-length - 60).toInt(), (-length + 40).toInt()).toFloat()
                drawLine(
                    color = furTextureColor,
                    start = Offset(x, y),
                    end = Offset(x, y + 15f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
