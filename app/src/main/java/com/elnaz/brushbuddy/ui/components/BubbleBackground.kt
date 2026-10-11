package com.elnaz.brushbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BubbleBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {

        val bubbles = listOf(
            Triple(0.15f, 0.12f, 35f),
            Triple(0.85f, 0.20f, 55f),
            Triple(0.25f, 0.45f, 45f),
            Triple(0.90f, 0.65f, 65f),
            Triple(0.15f, 0.85f, 50f),
            Triple(0.70f, 0.92f, 30f)
        )

        bubbles.forEach { (x, y, radius) ->
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = radius.dp.toPx(),
                center = Offset(
                    x = size.width * x,
                    y = size.height * y
                ),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}