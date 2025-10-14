package com.mafg.mafg.mafg.mafg.catsadventures.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GameCanvas(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        drawRect(
            color = Color.LightGray,
            size = size
        )

        drawCircle(
            color = Color.Red,
            center = Offset(size.width / 2, size.height / 4),
            radius = 80f
        )

        drawRect(
            color = Color.Blue,
            topLeft = Offset(size.width / 4, size.height / 2),
            size = Size(size.width / 2, 100f)
        )

        drawLine(
            color = Color.Green,
            start = Offset(size.width * 0.2f, size.height * 0.8f),
            end = Offset(size.width * 0.8f, size.height * 0.8f),
            strokeWidth = 8f
        )
    }
}