package com.elnaz.brushbuddy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elnaz.brushbuddy.R

@Composable
fun BrushNowButton(
    onClick: () -> Unit // TODO: Navigate to BrushingScreen
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00C9A7)
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.brush),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Brush Now",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
