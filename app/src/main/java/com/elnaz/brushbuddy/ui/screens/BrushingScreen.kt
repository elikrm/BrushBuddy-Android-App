package com.elnaz.brushbuddy.ui.screens


import android.R.attr.background
import android.R.id.background
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
import androidx.compose.runtime.remember
import com.elnaz.brushbuddy.models.BrushingStateEnum
import com.elnaz.brushbuddy.models.BrushingStatus

@Composable
fun BrushingScreen(
    brushingStatus: BrushingStatus,
    brushingTimeSeconds: Int,
    brushingState: BrushingStateEnum
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
            .background(Color(0xFFF3FCF9))
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

        // ADDED: Display brushing duration
        Text(
            text = "%02d:%02d".format(minutes, seconds),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00C9A7)
        )
    }
}
