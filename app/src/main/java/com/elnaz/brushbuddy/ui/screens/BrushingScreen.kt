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

@Composable
fun BrushingScreen(
    brushingTimeSeconds: Int
) {
    // ADDED: Convert brushing duration to minutes and seconds
    val minutes = brushingTimeSeconds / 60
    val seconds = brushingTimeSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3FCF9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
