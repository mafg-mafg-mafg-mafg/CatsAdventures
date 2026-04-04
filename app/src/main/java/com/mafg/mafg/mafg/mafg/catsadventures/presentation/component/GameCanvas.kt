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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Clases para cachear elementos estáticos
data class MountainCache(val path: Path, val snowPath: Path, val brush: Brush)
data class TreeCache(val trunkRect: Rect, val leafPath: Path, val leafColor: Color)

@Composable
fun GameCanvas(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
) {
    val state by viewModel.gameState.collectAsState()
    val context = LocalContext.current
    
    var time by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val pawExtension = remember { Animatable(0f) }
    val beeAlpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    var targetPos by remember { mutableStateOf(Offset.Zero) }
    var currentBeePos by remember { mutableStateOf(Offset.Zero) }
    var isBeeCaught by remember { mutableStateOf(false) }
    
    val trailPositions = remember { mutableStateListOf<Offset>() }

    // Gradiente de fondo
    val sunsetGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A237E), Color(0xFF3F51B5), Color(0xFF9575CD), Color(0xFFFF7043), Color(0xFFFFAB91)
            )
        )
    }

    val mountainCache = remember(canvasSize) {
        if (canvasSize == IntSize.Zero) emptyList()
        else createMountainCache(canvasSize.width.toFloat(), canvasSize.height.toFloat())
    }

    val treeCache = remember(canvasSize) {
        if (canvasSize == IntSize.Zero) emptyList()
        else createTreeCache(canvasSize.width.toFloat(), canvasSize.height.toFloat())
    }

    val formattedTime by remember {
        derivedStateOf {
            val totalSeconds = state.timeLeft.toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hundredths = ((state.timeLeft - totalSeconds) * 100).toInt()
            "%02d:%02d:%02d".format(minutes, seconds, hundredths)
        }
    }

    // Feedback
    fun triggerFunVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 100, 80), intArrayOf(0, 255, 0, 255), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 60, 100, 80), -1)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun playFestiveSound() {
        scope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGen.startTone(ToneGenerator.TONE_DTMF_S, 150)
                delay(200)
                toneGen.startTone(ToneGenerator.TONE_DTMF_P, 200)
                toneGen.release()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (state.timeLeft > 0f) {
                delay(10)
                viewModel.onEvent(GameEvent.DecrementTime)
            } else delay(100)
        }
    }

    // --- MOVIMIENTO DE LA ABEJA: ESTILO PLUMA MUY RÁPIDO ---
    LaunchedEffect(Unit) {
        var lastFrameTime = System.nanoTime()
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
                time += deltaTime

                if (canvasSize != IntSize.Zero && !isBeeCaught) {
                    val cw = canvasSize.width.toFloat()
                    val ch = canvasSize.height.toFloat()
                    val halfHeight = ch / 2f
                    
                    // Movimiento horizontal aún más rápido
                    val dx = sin(time * 1.3f) * (cw * 0.35f) + cos(time * 0.7f) * (cw * 0.07f)
                    
                    // Movimiento vertical más enérgico que toca la línea roja frecuentemente
                    val vTime = time * 2.2f + sin(time * 1.2f) * 0.8f
                    val verticalOscillation = (cos(vTime) + 1f) / 2f
                    val dy = verticalOscillation * (halfHeight * 0.9f)
                    
                    currentBeePos = Offset(
                        x = (cw / 2) + dx,
                        y = (halfHeight * 0.1f) + dy 
                    )

                    if (beeAlpha.value > 0.01f) {
                        trailPositions.add(currentBeePos)
                        if (trailPositions.size > 20) trailPositions.removeAt(0)
                    } else if (trailPositions.isNotEmpty()) trailPositions.clear()
                } else if (isBeeCaught) trailPositions.clear()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(sunsetGradient)) {
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp)
        )

        val scoreScale by animateFloatAsState(targetValue = 1f, label = "scoreScale")
        Text(
            text = "${state.score}",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).graphicsLayer {
                scaleX = scoreScale; scaleY = scoreScale
            }
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            if (!pawExtension.isRunning && state.score > 0 && state.timeLeft > 0 && beeAlpha.value > 0.9f) {
                                targetPos = tapOffset
                                scope.launch {
                                    pawExtension.animateTo(1f, tween(250, easing = FastOutSlowInEasing))
                                    
                                    val reachY = maxOf(targetPos.y, canvasSize.height / 2f)
                                    val effectiveReachPos = Offset(targetPos.x, reachY)
                                    
                                    if ((currentBeePos - effectiveReachPos).getDistance() < 130f) {
                                        isBeeCaught = true
                                        triggerFunVibration(context)
                                        playFestiveSound()
                                        viewModel.onEvent(GameEvent.DecrementScore)
                                    }
                                    
                                    pawExtension.animateTo(0f, tween(350, easing = LinearOutSlowInEasing))
                                    if (isBeeCaught) {
                                        beeAlpha.animateTo(0f, tween(100))
                                        isBeeCaught = false
                                        delay(400)
                                        beeAlpha.animateTo(1f, tween(300))
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            val cw = size.width; val ch = size.height

            // 0. Línea roja límite
            drawLine(Color.Red, start = Offset(0f, ch / 2), end = Offset(cw, ch / 2), strokeWidth = 5f)

            // 1. Elementos de fondo
            drawSun(cw, ch, time)
            drawClouds(cw, ch, time)
            mountainCache.forEach { m ->
                drawPath(m.path, brush = m.brush)
                drawPath(m.snowPath, color = Color(0xFFFCE4EC).copy(alpha = 0.8f))
            }

            // 2. Bosque
            drawRect(color = Color(0xFF1B5E20), topLeft = Offset(0f, ch - 120f), size = Size(cw, 120f))
            treeCache.forEach { t ->
                drawRect(color = Color(0xFF21100D), topLeft = t.trunkRect.topLeft, size = t.trunkRect.size)
                drawPath(t.leafPath, color = t.leafColor)
            }

            // 3. Estela de la abeja
            trailPositions.forEachIndexed { index, pos ->
                val alpha = (index.toFloat() / trailPositions.size) * 0.3f * beeAlpha.value
                drawCircle(Color.White.copy(alpha = alpha), center = pos, radius = 10f * (index.toFloat() / trailPositions.size))
            }

            val startPos = Offset(cw / 2, ch)
            val restingPos = Offset(cw / 2, ch - 80f)
            
            val currentTipPos = if (pawExtension.value > 0.01f) {
                val fullPos = startPos + (targetPos - startPos) * pawExtension.value
                Offset(fullPos.x, maxOf(fullPos.y, ch / 2f))
            } else {
                restingPos
            }
            
            val beeDrawPos = if (isBeeCaught) currentTipPos else currentBeePos
            if (beeAlpha.value > 0.01f) drawBee(beeDrawPos, 70f, beeAlpha.value, time)

            drawRealisticPaw(startPos, currentTipPos)
        }
    }
}

// --- FUNCIONES DE DIBUJO ---

fun DrawScope.drawSun(cw: Float, ch: Float, time: Float) {
    val sunCenter = Offset(cw * 0.5f, ch * 0.6f); val pulse = (sin(time * 1.5f) + 1f) * 15f
    drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFFFF7043).copy(alpha = 0.5f), Color.Transparent), center = sunCenter, radius = 300f + pulse), center = sunCenter, radius = 300f + pulse)
    drawCircle(brush = Brush.linearGradient(colors = listOf(Color(0xFFFFEB3B), Color(0xFFF4511E)), start = Offset(sunCenter.x, sunCenter.y - 100f), end = Offset(sunCenter.x, sunCenter.y + 100f)), center = sunCenter, radius = 100f)
}

fun DrawScope.drawClouds(cw: Float, ch: Float, time: Float) {
    val speed = 15f
    fun drawCloud(x: Float, y: Float, scale: Float) {
        val c = Color(0xFFFFCCBC).copy(alpha = 0.6f); val s = 40f * scale
        drawCircle(c, center = Offset(x, y), radius = s); drawCircle(c, center = Offset(x + s * 0.8f, y - s * 0.2f), radius = s * 0.8f); drawCircle(c, center = Offset(x - s * 0.8f, y - s * 0.2f), radius = s * 0.8f)
    }
    drawCloud((time * speed + 100f) % (cw + 200f) - 100f, ch * 0.15f, 1.2f)
    drawCloud((time * speed * 0.7f + cw * 0.5f) % (cw + 300f) - 150f, ch * 0.35f, 0.9f)
}

fun DrawScope.drawBee(center: Offset, size: Float, alpha: Float, time: Float) {
    val wingFlap = sin(time * 50f) * 20f; val wingColor = Color.White.copy(alpha = 0.6f * alpha)
    rotate(45f + wingFlap, pivot = center) { drawOval(wingColor, topLeft = Offset(center.x - size * 0.8f, center.y - size * 0.5f), size = Size(size * 0.8f, size * 0.4f)) }
    rotate(-45f - wingFlap, pivot = center) { drawOval(wingColor, topLeft = Offset(center.x, center.y - size * 0.5f), size = Size(size * 0.8f, size * 0.4f)) }
    drawOval(Color(0xFFFFD600).copy(alpha = alpha), topLeft = Offset(center.x - size / 2, center.y - size * 0.3f), size = Size(size, size * 0.6f))
    repeat(3) { i -> val xOff = (i - 1) * (size * 0.25f); drawRect(Color.Black.copy(alpha = alpha), topLeft = Offset(center.x + xOff - 2f, center.y - size * 0.28f), size = Size(size * 0.15f, size * 0.56f)) }
}

fun DrawScope.drawRealisticPaw(start: Offset, end: Offset) {
    val vector = end - start; val angle = if (vector.getDistance() < 1f) 0f else Math.toDegrees(atan2(vector.y.toDouble(), vector.x.toDouble())).toFloat() + 90f; val length = vector.getDistance()
    translate(start.x, start.y) {
        rotate(angle, pivot = Offset.Zero) {
            val armPath = Path().apply { moveTo(-60f, 0f); lineTo(-48f, -length); quadraticTo(0f, -length - 40f, 48f, -length); lineTo(60f, 0f); close() }
            drawPath(armPath, brush = Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color.Black, Color(0xFF333333)))); drawOval(Color.Black, topLeft = Offset(-80f, -length - 70f), size = Size(160f, 140f))
            val padColor = Color(0xFFFFB7C5); val toeOffsets = arrayOf(Offset(-60f, -length - 20f), Offset(-25f, -length - 50f), Offset(25f, -length - 50f), Offset(60f, -length - 20f))
            toeOffsets.forEach { pos -> drawCircle(Color.Black, center = pos, radius = 35f); drawCircle(padColor, center = pos + Offset(0f, -5f), radius = 21f) }
            val mainPadPath = Path().apply { moveTo(0f, -length + 30f); cubicTo(-50f, -length + 10f, -40f, -length + 70f, 0f, -length + 70f); cubicTo(40f, -length + 70f, 50f, -length + 10f, 0f, -length + 30f); close() }
            drawPath(mainPadPath, color = padColor)
        }
    }
}

// --- FUNCIONES DE CACHE ---

private fun createMountainCache(cw: Float, ch: Float): List<MountainCache> {
    val groundY = ch - 120f; val caches = mutableListOf<MountainCache>()
    fun addMountain(baseStart: Float, peakX: Float, peakY: Float, baseEnd: Float, color: Color, seed: Int) {
        val random = Random(seed); val path = Path().apply {
            moveTo(baseStart, groundY)
            for (i in 1..4) { val t = i.toFloat() / 5; lineTo(baseStart + (peakX - baseStart) * t, groundY - (groundY - peakY) * t + (random.nextFloat() - 0.5f) * 40f) }
            lineTo(peakX, peakY)
            for (i in 1..4) { val t = i.toFloat() / 5; lineTo(peakX + (baseEnd - peakX) * t, peakY + (groundY - peakY) * t + (random.nextFloat() - 0.5f) * 40f) }
            lineTo(baseEnd, groundY); close()
        }
        val mountainColor = color.compositeOver(Color(0xFF5E35B1).copy(alpha = 0.3f))
        val brush = Brush.linearGradient(colors = listOf(mountainColor.compositeOver(Color.White.copy(0.2f)), mountainColor.compositeOver(Color.Black.copy(0.3f))), start = Offset(baseStart, peakY), end = Offset(baseEnd, groundY))
        val snowPath = Path().apply {
            moveTo(peakX - (peakX - baseStart) * 0.15f, peakY + (groundY - peakY) * 0.15f); lineTo(peakX, peakY); lineTo(peakX + (baseEnd - peakX) * 0.15f, peakY + (groundY - peakY) * 0.15f)
            for (i in 1..5) { val t = i.toFloat() / 5; lineTo(peakX + (baseEnd - peakX) * 0.15f - ((baseEnd - peakX) * 0.3f) * t, peakY + (groundY - peakY) * 0.15f + (random.nextFloat() * 20f)) }
            close()
        }
        caches.add(MountainCache(path, snowPath, brush))
    }
    addMountain(-200f, cw * 0.2f, groundY - 350f, cw * 0.5f, Color(0xFF78909C), 789)
    addMountain(cw * 0.5f, cw * 0.85f, groundY - 300f, cw * 1.3f, Color(0xFF90A4AE), 101)
    addMountain(cw * 0.3f, cw * 0.65f, groundY - 500f, cw * 1.1f, Color(0xFF546E7A), 456)
    addMountain(-50f, cw * 0.45f, groundY - 250f, cw * 0.95f, Color(0xFF455A64), 123)
    return caches
}

private fun createTreeCache(cw: Float, ch: Float): List<TreeCache> {
    val groundY = ch - 120f; val caches = mutableListOf<TreeCache>(); val random = Random(123)
    repeat(9) { val x = random.nextFloat() * cw; val scale = 0.5f + random.nextFloat() * 0.5f; caches.add(createSingleTree(x, groundY + 20f, scale, Color(0xFF0D2E10))) }
    val randomNear = Random(456); repeat(5) { i -> val x = (cw / 5) * i + randomNear.nextFloat() * (cw / 10); val scale = 0.8f + randomNear.nextFloat() * 0.4f; caches.add(createSingleTree(x, groundY + 50f, scale, Color(0xFF1B5E20))) }
    return caches
}

private fun createSingleTree(x: Float, groundY: Float, scale: Float, color: Color): TreeCache {
    val tw = 15f * scale; val th = 30f * scale; val lw = 60f * scale; val lh = 80f * scale
    val trunkRect = Rect(x - tw / 2, groundY - th, x + tw / 2, groundY)
    val leafPath = Path().apply { moveTo(x, groundY - th - lh); lineTo(x - lw / 2, groundY - th); lineTo(x + lw / 2, groundY - th); close() }
    return TreeCache(trunkRect, leafPath, color)
}
