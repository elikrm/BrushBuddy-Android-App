package com.elnaz.brushbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.random.Random

// A class to hold each circle's properties
class MovingCircle(
    x: Float,
    y: Float,
    var vx: Float, // Velocity X
    var vy: Float, // Velocity Y
    val radius: Float,
    val color: Color
) {
    //X position observable by Compose
    var x by mutableFloatStateOf(x)
    //Y position observable by Compose
    var y by mutableFloatStateOf(y)
}
@Composable
fun RandomFloatingCirclesBackground() {
    // keeping track of circles
    val circles = remember { mutableStateListOf<MovingCircle>() }
    //Storing the actual Canvas width
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    //Storing the actual Canvas height
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    //Game loop using withFrameMillis for smooth, frame-rate independent movement
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { time ->
                circles.forEach { circle ->
                    // Moving the circle slowly
                    circle.x += circle.vx
                    circle.y += circle.vy
                    // Using actual Canvas width
                    if (circle.x - circle.radius < 0 ||
                        circle.x + circle.radius > canvasWidth) {
                        circle.vx *= -1
                    }
                    // Using actual Canvas height
                    if (circle.y - circle.radius < 0 ||
                        circle.y + circle.radius > canvasHeight) {
                        circle.vy *= -1
                    }
                }
            }
        }
    }
    //Initializing circles outside the Canvas drawing block
    LaunchedEffect(canvasWidth, canvasHeight) {
        if (canvasWidth > 0f && canvasHeight > 0f && circles.isEmpty()) {
            val width = canvasWidth
            val height = canvasHeight

            repeat(15) { // Number of circles
                circles.add(
                    MovingCircle(
                        x = Random.nextFloat() * width,
                        y = Random.nextFloat() * height,
                        vx = (Random.nextFloat() * 2f - 1f) * 1.5f,
                        vy = (Random.nextFloat() * 2f - 1f) * 1.5f,
                        radius = Random.nextFloat() * 200f + 50f,//Radius between 50 and 250 pixels
                        color = Color(0xFF00C9A7).copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
    Canvas(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged { size -> //Getting actual Canvas size
            canvasWidth = size.width.toFloat()
            canvasHeight = size.height.toFloat()
        }) {
            circles.forEach { circle ->
                drawCircle(
                    color = circle.color,
                    radius = circle.radius,
                    center = Offset(circle.x, circle.y),
                    style = Fill
                )
            }
    }
}
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
                color = Color(0xFF00C9A7).copy(alpha = 0.30f),
                radius = radius.dp.toPx(),
                center = Offset(
                    x = size.width * x,
                    y = size.height * y
                ),
                style = Fill
            )
        }
    }
}