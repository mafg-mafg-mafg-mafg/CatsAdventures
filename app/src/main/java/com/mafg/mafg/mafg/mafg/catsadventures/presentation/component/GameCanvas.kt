package com.mafg.mafg.mafg.mafg.catsadventures.presentation.component

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val context = LocalContext.current
    
    var time by remember { mutableStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val pawExtension = remember { Animatable(0f) }
    val beeAlpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    var targetPos by remember { mutableStateOf(Offset.Zero) }
    var currentBeePos by remember { mutableStateOf(Offset.Zero) }
    
    var isBeeCaught by remember { mutableStateOf(false) }

    // Función para vibración mejorada
    fun triggerFunVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 60, 100, 80)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 60, 100, 80), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Función para sonido festivo
    fun playFestiveSound() {
        try {
            scope.launch {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGen.startTone(ToneGenerator.TONE_DTMF_S, 150)
                delay(200)
                toneGen.startTone(ToneGenerator.TONE_DTMF_P, 200)
                toneGen.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (state.timeLeft > 0f) {
                delay(10)
                viewModel.onEvent(GameEvent.DecrementTime)
            } else {
                delay(100)
            }
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

    val sunsetGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A237E), Color(0xFF3F51B5), Color(0xFF9575CD), Color(0xFFFF7043), Color(0xFFFFAB91)
        )
    )

    Box(modifier = modifier.fillMaxSize().background(sunsetGradient)) {
        val timeLeftScale = remember { Animatable(1f) }
        val currentSecond = state.timeLeft.toInt()
        
        LaunchedEffect(currentSecond) {
            if (state.timeLeft > 0f) {
                timeLeftScale.animateTo(1.3f, tween(100))
                timeLeftScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
            }
        }
        
        val totalSeconds = state.timeLeft.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = ((state.timeLeft - totalSeconds) * 100).toInt()

        Text(
            text = "%02d:%02d:%02d".format(minutes, seconds, hundredths),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp).graphicsLayer {
                scaleX = timeLeftScale.value
                scaleY = timeLeftScale.value
            }
        )

        val scoreScale = remember { Animatable(1f) }
        LaunchedEffect(state.score) {
            scoreScale.animateTo(1.6f, tween(100))
            scoreScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
        }

        Text(
            text = "${state.score}",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).graphicsLayer {
                scaleX = scoreScale.value
                scaleY = scoreScale.value
            }
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!pawExtension.isRunning && state.score > 0 && state.timeLeft > 0 && beeAlpha.value > 0.9f) {
                                targetPos = currentBeePos
                                scope.launch {
                                    pawExtension.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
                                    val startPos = Offset(canvasSize.width / 2f, canvasSize.height.toFloat())
                                    val tipPos = startPos + (targetPos - startPos) * 1f
                                    
                                    if ((currentBeePos - tipPos).getDistance() < 120f) {
                                        isBeeCaught = true
                                        triggerFunVibration(context)
                                        playFestiveSound()
                                        viewModel.onEvent(GameEvent.DecrementScore)
                                    }
                                    
                                    pawExtension.animateTo(0f, tween(400, easing = LinearOutSlowInEasing))
                                    if (isBeeCaught) {
                                        beeAlpha.animateTo(0f, tween(150))
                                        isBeeCaught = false
                                        delay(300)
                                        beeAlpha.animateTo(1f, tween(300))
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            val cw = size.width
            val ch = size.height
            drawSun(cw, ch, time)
            drawClouds(cw, ch, time)
            drawImprovedMountains(cw, ch)
            drawForest(cw, ch)

            val angleDegrees = 45f * sin(time * 3f)
            val vOffset = 200f + 200f * sin(time * 2f)
            val anchor = Offset(cw / 2, 20f + vOffset)
            val ropeLength = ch * 0.3f
            val angleRadians = (angleDegrees + 90f) * (PI.toFloat() / 180f)
            val pendulumBeePos = Offset(x = anchor.x + ropeLength * cos(angleRadians), y = anchor.y + ropeLength * sin(angleRadians))

            if (!isBeeCaught) currentBeePos = pendulumBeePos
            val beeSize = 70f
            
            // --- ESTELA MEJORADA ---
            if (!isBeeCaught && beeAlpha.value > 0.01f) {
                trailPositions.add(currentBeePos)
                // Estela más larga: mantenemos 25 posiciones en lugar de 10
                if (trailPositions.size > 25) trailPositions.removeAt(0)
                
                trailPositions.forEachIndexed { index, position ->
                    val alpha = (index.toFloat() / trailPositions.size) * 0.4f * beeAlpha.value
                    // Círculos blancos más evidentes y con mayor radio progresivo
                    drawCircle(
                        Color.White.copy(alpha = alpha), 
                        center = position, 
                        radius = 15f * (index.toFloat() / trailPositions.size)
                    )
                }
            } else trailPositions.clear()

            val startPos = Offset(cw / 2, ch)
            val restingPos = Offset(cw / 2, ch - 80f)
            val currentTipPos = if (pawExtension.value > 0.01f) startPos + (targetPos - startPos) * pawExtension.value else restingPos
            val lineEnd = if (pawExtension.isRunning) targetPos else currentBeePos
            
            if (beeAlpha.value > 0.9f) {
                val rainbowBrush = Brush.linearGradient(colors = listOf(Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red), start = startPos, end = lineEnd)
                drawLine(brush = rainbowBrush, start = startPos, end = lineEnd, strokeWidth = 4f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            }
            
            if (isBeeCaught) currentBeePos = currentTipPos
            if (beeAlpha.value > 0.01f) drawBee(center = currentBeePos, size = beeSize, alpha = beeAlpha.value, time = time)
            drawRealisticPaw(startPos, currentTipPos)
        }
    }
}

fun DrawScope.drawSun(cw: Float, ch: Float, time: Float) {
    val sunCenter = Offset(cw * 0.5f, ch * 0.6f) 
    val sunRadius = 100f
    val pulse = (sin(time * 1.5f) + 1f) * 15f
    drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFFFF7043).copy(alpha = 0.5f), Color.Transparent), center = sunCenter, radius = sunRadius * 3f + pulse), center = sunCenter, radius = sunRadius * 3f + pulse)
    drawCircle(brush = Brush.linearGradient(colors = listOf(Color(0xFFFFEB3B), Color(0xFFF4511E)), start = Offset(sunCenter.x, sunCenter.y - sunRadius), end = Offset(sunCenter.x, sunCenter.y + sunRadius)), center = sunCenter, radius = sunRadius)
}

fun DrawScope.drawClouds(cw: Float, ch: Float, time: Float) {
    fun drawCloud(x: Float, y: Float, scale: Float) {
        val cloudColor = Color(0xFFFFCCBC).copy(alpha = 0.6f)
        val cloudSize = 40f * scale
        drawCircle(cloudColor, center = Offset(x, y), radius = cloudSize)
        drawCircle(cloudColor, center = Offset(x + cloudSize * 0.8f, y - cloudSize * 0.2f), radius = cloudSize * 0.8f)
        drawCircle(cloudColor, center = Offset(x - cloudSize * 0.8f, y - cloudSize * 0.2f), radius = cloudSize * 0.8f)
        drawCircle(cloudColor, center = Offset(x + cloudSize * 1.4f, y + cloudSize * 0.1f), radius = cloudSize * 0.6f)
        drawCircle(cloudColor, center = Offset(x - cloudSize * 1.4f, y + cloudSize * 0.1f), radius = cloudSize * 0.6f)
    }
    val speed = 15f
    drawCloud((time * speed + 100f) % (cw + 200f) - 100f, ch * 0.15f, 1.2f)
    drawCloud((time * speed * 0.7f + cw * 0.5f) % (cw + 300f) - 150f, ch * 0.35f, 0.9f)
    drawCloud((time * speed * 1.2f + cw * 0.8f) % (cw + 250f) - 120f, ch * 0.22f, 1.0f)
}

fun DrawScope.drawBee(center: Offset, size: Float, alpha: Float, time: Float) {
    val bodyColor = Color(0xFFFFD600)
    val stripeColor = Color.Black
    val wingColor = Color.White.copy(alpha = 0.6f * alpha)
    val wingFlap = sin(time * 50f) * 20f
    rotate(45f + wingFlap, pivot = center) { drawOval(color = wingColor, topLeft = Offset(center.x - size * 0.8f, center.y - size * 0.5f), size = Size(size * 0.8f, size * 0.4f)) }
    rotate(-45f - wingFlap, pivot = center) { drawOval(color = wingColor, topLeft = Offset(center.x, center.y - size * 0.5f), size = Size(size * 0.8f, size * 0.4f)) }
    drawOval(color = bodyColor.copy(alpha = alpha), topLeft = Offset(center.x - size / 2, center.y - size * 0.3f), size = Size(size, size * 0.6f))
    for (i in -1..1) {
        val xOffset = i * (size * 0.25f)
        drawRect(color = stripeColor.copy(alpha = alpha), topLeft = Offset(center.x + xOffset - 2f, center.y - size * 0.28f), size = Size(size * 0.15f, size * 0.56f))
    }
    drawCircle(color = Color.Black.copy(alpha = alpha), center = center + Offset(size * 0.35f, -size * 0.05f), radius = size * 0.08f)
}

fun DrawScope.drawImprovedMountains(cw: Float, ch: Float) {
    val groundY = ch - 120f
    fun drawMountainShape(baseStart: Float, peakX: Float, peakY: Float, baseEnd: Float, color: Color, seed: Int) {
        val random = Random(seed)
        val path = Path().apply {
            moveTo(baseStart, groundY)
            val segmentsLeft = 4
            for (i in 1..segmentsLeft) {
                val t = i.toFloat() / (segmentsLeft + 1)
                val x = baseStart + (peakX - baseStart) * t
                val y = groundY - (groundY - peakY) * t + (random.nextFloat() - 0.5f) * 40f
                lineTo(x, y)
            }
            lineTo(peakX, peakY)
            val segmentsRight = 4
            for (i in 1..segmentsRight) {
                val t = i.toFloat() / (segmentsRight + 1)
                val x = peakX + (baseEnd - peakX) * t
                val y = peakY + (groundY - peakY) * t + (random.nextFloat() - 0.5f) * 40f
                lineTo(x, y)
            }
            lineTo(baseEnd, groundY)
            close()
        }
        val mountainColor = color.compositeOver(Color(0xFF5E35B1).copy(alpha = 0.3f))
        drawPath(path = path, brush = Brush.linearGradient(colors = listOf(mountainColor.compositeOver(Color.White.copy(0.2f)), mountainColor.compositeOver(Color.Black.copy(0.3f))), start = Offset(baseStart, peakY), end = Offset(baseEnd, groundY)))
        val snowPath = Path().apply {
            moveTo(peakX - (peakX - baseStart) * 0.15f, peakY + (groundY - peakY) * 0.15f)
            lineTo(peakX, peakY)
            lineTo(peakX + (baseEnd - peakX) * 0.15f, peakY + (groundY - peakY) * 0.15f)
            val steps = 5
            for (i in 1..steps) {
                val t = i.toFloat() / steps
                val x = peakX + (baseEnd - peakX) * 0.15f - ( (baseEnd - peakX) * 0.15f + (peakX - baseStart) * 0.15f ) * t
                val y = peakY + (groundY - peakY) * 0.15f + (random.nextFloat() * 20f)
                lineTo(x, y)
            }
            close()
        }
        drawPath(snowPath, color = Color(0xFFFCE4EC).copy(alpha = 0.8f))
    }
    drawMountainShape(-200f, cw * 0.2f, groundY - 350f, cw * 0.5f, Color(0xFF78909C), 789)
    drawMountainShape(cw * 0.5f, cw * 0.85f, groundY - 300f, cw * 1.3f, Color(0xFF90A4AE), 101)
    drawMountainShape(cw * 0.3f, cw * 0.65f, groundY - 500f, cw * 1.1f, Color(0xFF546E7A), 456)
    drawMountainShape(-50f, cw * 0.45f, groundY - 250f, cw * 0.95f, Color(0xFF455A64), 123)
}

fun DrawScope.drawForest(cw: Float, ch: Float) {
    val groundHeight = 120f
    drawRect(color = Color(0xFF1B5E20), topLeft = Offset(0f, ch - groundHeight), size = Size(cw, groundHeight))
    val randomDist = Random(123)
    for (i in 0..8) {
        val x = randomDist.nextFloat() * cw
        val scale = 0.5f + randomDist.nextFloat() * 0.5f
        drawTree(x, ch - groundHeight + 20f, scale, Color(0xFF0D2E10))
    }
    val randomNear = Random(456)
    for (i in 0..4) {
        val x = (cw / 5) * i + randomNear.nextFloat() * (cw / 10)
        val scale = 0.8f + randomNear.nextFloat() * 0.4f
        drawTree(x, ch - groundHeight + 50f, scale, Color(0xFF1B5E20))
    }
}

fun DrawScope.drawTree(x: Float, groundY: Float, scale: Float, leafColor: Color) {
    val trunkWidth = 15f * scale
    val trunkHeight = 30f * scale
    val leavesWidth = 60f * scale
    val leavesHeight = 80f * scale
    drawRect(color = Color(0xFF21100D), topLeft = Offset(x - trunkWidth / 2, groundY - trunkHeight), size = Size(trunkWidth, trunkHeight))
    val path = Path().apply {
        moveTo(x, groundY - trunkHeight - leavesHeight)
        lineTo(x - leavesWidth / 2, groundY - trunkHeight)
        lineTo(x + leavesWidth / 2, groundY - trunkHeight)
        close()
    }
    drawPath(path, color = leafColor)
}

fun DrawScope.drawRealisticPaw(start: Offset, end: Offset) {
    val vector = end - start
    val angle = if (vector.getDistance() < 1f) 0f else { Math.toDegrees(atan2(vector.y.toDouble(), vector.x.toDouble())).toFloat() + 90f }
    val length = vector.getDistance()
    val pawColor = Color.Black
    val shadowColor = Color(0xFF212121)
    val padColor = Color(0xFFFFB7C5)
    val furTextureColor = Color.White.copy(alpha = 0.1f)
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
            drawPath(path = armPath, brush = Brush.linearGradient(colors = listOf(Color(0xFF1A1A1A), pawColor, Color(0xFF333333)), start = Offset(0f, 0f), end = Offset(0f, -length)))
            val headWidth = 160f
            val headHeight = 140f
            val headOffset = -length
            drawOval(color = pawColor, topLeft = Offset(-headWidth / 2, headOffset - headHeight / 2), size = Size(headWidth, headHeight))
            val toeRadius = 35f
            val toeOffsets = listOf(Offset(-60f, headOffset - 20f), Offset(-25f, headOffset - 50f), Offset(25f, headOffset - 50f), Offset(60f, headOffset - 20f))
            toeOffsets.forEach { pos ->
                drawCircle(color = shadowColor, center = pos + Offset(0f, 5f), radius = toeRadius)
                drawCircle(color = pawColor, center = pos, radius = toeRadius)
                drawCircle(color = padColor, center = pos + Offset(0f, -5f), radius = toeRadius * 0.6f)
                val clawPath = Path().apply { moveTo(pos.x - 4f, pos.y - toeRadius + 5f); lineTo(pos.x, pos.y - toeRadius - 25f); lineTo(pos.x + 4f, pos.y - toeRadius + 5f); close() }
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
                drawLine(color = furTextureColor, start = Offset(x, y), end = Offset(x, y + 15f), strokeWidth = 2f, cap = StrokeCap.Round)
            }
        }
    }
}
