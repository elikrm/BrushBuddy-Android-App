package com.elnaz.brushbuddy.ui.screens


import android.R.attr.background
import android.R.id.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.elnaz.brushbuddy.models.BrushingStateEnum
import com.elnaz.brushbuddy.models.BrushingStatus
import com.elnaz.brushbuddy.ui.components.BubbleBackground
import com.elnaz.brushbuddy.ui.components.RandomFloatingCirclesBackground

private val AppBackground = Color(0xFFF3FCF9)
@Composable
fun CircularTimer(
    minutes: Int,
    seconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier
) {
    val remainingSeconds = (minutes * 60) + seconds

    //Calculate progress fraction (between 0f and 1f)
    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()

            // Background track arc
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active progress arc
            drawArc(
                color = Color(0xFF00C9A7),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "%02d:%02d".format(minutes, seconds),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00C9A7)
        )
    }
}
@Composable
fun BrushingScreen(
    brushingStatus: BrushingStatus,
    brushingTimeSeconds: Int,
    brushingState: BrushingStateEnum
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        RandomFloatingCirclesBackground()
        // Foreground layer
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ADDED: Convert brushing duration to minutes and seconds
            val minutes = brushingTimeSeconds / 60
            val seconds = brushingTimeSeconds % 60
//    val messageToUser = when (brushingStatus) {
//        BrushingStatus.BRUSHING -> "Keep going! You need at least 30 seconds"
//        BrushingStatus.IDLE -> "Ready to brush"
//    }
            val messageToUser = remember(brushingState)
            {
                when (brushingState) {
                    BrushingStateEnum.NOT_BRUSHING -> {"Ready to brush"}
                    BrushingStateEnum.EARLY -> "Starting brushing..."
                    BrushingStateEnum.BRUSHING ->{"Keep going! You need at least 30 seconds"}
                    BrushingStateEnum.PAUSED -> "Brushing paused"
                    BrushingStateEnum.CONFIRMED_SESSION -> {"Great job!"}
                    BrushingStateEnum.ABORTED_SESSION -> {"Session too short"}
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Brushing State: ${brushingState.name}",
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = messageToUser, // CHANGED
                    color = Color(0xFF191B2C)
                )
                Text(
                    text = "Brushing Time",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191B2C)
                )

                Spacer(modifier = Modifier.height(24.dp))

                val totalSeconds = 120
                CircularTimer(
                    minutes,
                    seconds,
                    totalSeconds,
                )
            }
        }
    }
}
