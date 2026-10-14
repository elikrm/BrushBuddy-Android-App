
package com.elnaz.brushbuddy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elnaz.brushbuddy.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyStreakCard() {

    // Get today's date
    val today = LocalDate.now()

    // Get the last 7 days, including today
    val days = (6 downTo 0).map {
        today.minusDays(it.toLong())
    }

    // ADDED: White card containing the entire weekly streak
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        // ADDED: Column puts the header ABOVE the weekday circles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            // FIRST ROW: Flame icon and streak count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.streak),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "0 day streak", // TODO: Use actual streak count
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191B2C)
                )
            }

            // SECOND ROW: Seven circles and weekday labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Grey circle for each day
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFFE0E5EB),
                                    CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = day.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.ENGLISH
                            ),
                            fontSize = 12.sp,
                            color = Color(0xFF596170)
                        )
                    }
                }
            } // End weekday Row

        } // End Column
    } // End Card
}