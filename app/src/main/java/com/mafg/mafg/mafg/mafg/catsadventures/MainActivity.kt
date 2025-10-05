package com.mafg.mafg.mafg.mafg.catsadventures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.mafg.mafg.mafg.mafg.catsadventures.ui.theme.CatsAdventuresTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatsAdventuresTheme {
                AppNavigation()
            }
        }
    }
}

// Estado observable para navegación
object AppState {
    var currentScreen by mutableStateOf("main") // "main", "chapter" o "canvas"
    var currentChapter by mutableIntStateOf(1)
}

@Composable
fun AppNavigation() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (AppState.currentScreen) {
            "main" -> PlayButton(
                modifier = Modifier.padding(innerPadding)
            )
            "chapter" -> ChapterScreen(
                chapterNumber = AppState.currentChapter,
                modifier = Modifier.padding(innerPadding)
            )
            "canvas" -> DrawingCanvas(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun PlayButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                // Navegar a la pantalla del capítulo
                AppState.currentScreen = "chapter"
                AppState.currentChapter = 1
            }
        ) {
            Text("Play")
        }
    }
}

@Composable
fun ChapterScreen(chapterNumber: Int, modifier: Modifier = Modifier) {
    // Efecto para cambiar automáticamente después de 3 segundos
    LaunchedEffect(key1 = chapterNumber) {
        delay(3000) // Espera 3 segundos (3000 milisegundos)
        AppState.currentScreen = "canvas" // Navega a la pantalla Canvas
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Capítulo $chapterNumber",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun DrawingCanvas(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Canvas para dibujar
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Fondo del canvas
            drawRect(
                color = Color.LightGray,
                size = size
            )

            // Dibujar un círculo rojo
            drawCircle(
                color = Color.Red,
                center = Offset(size.width / 2, size.height / 4),
                radius = 80f
            )

            // Dibujar un rectángulo azul
            drawRect(
                color = Color.Blue,
                topLeft = Offset(size.width / 4, size.height / 2),
                size = Size(size.width / 2, 100f)
            )

            // Dibujar una línea verde
            drawLine(
                color = Color.Green,
                start = Offset(size.width * 0.2f, size.height * 0.8f),
                end = Offset(size.width * 0.8f, size.height * 0.8f),
                strokeWidth = 8f
            )

            // Dibujar texto "¡Bienvenido al Canvas!"
            // Nota: Para texto más avanzado, usa drawIntoCanvas con Paint
        }

        // Texto informativo
        Text(
            text = "Pantalla Canvas - Aquí puedes dibujar",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayButtonPreview() {
    CatsAdventuresTheme {
        PlayButton()
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterScreenPreview() {
    CatsAdventuresTheme {
        ChapterScreen(chapterNumber = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun DrawingCanvasPreview() {
    CatsAdventuresTheme {
        DrawingCanvas()
    }
}