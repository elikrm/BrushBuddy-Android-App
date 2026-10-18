package com.elnaz.brushbuddy.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import com.elnaz.brushbuddy.models.BrushingStatus
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.elnaz.brushbuddy.R
import com.elnaz.brushbuddy.data.local.BrushingSession
import com.elnaz.brushbuddy.ui.theme.BrushBuddyTheme
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale

// BrushMint-inspired palette
private val AppBackground = Color(0xFFF3FCF9)
private val CardBackground = Color(0xFFFFFFFF)
private val PrimaryMint = Color(0xFF00C9A7)
private val SecondaryBlue = Color(0xFF168FE5)
private val SoftMint = Color(0xFFD9F8EF)
private val HeadingText = Color(0xFF191B2C)
private val SecondaryText = Color(0xFF596170)
private val CardBorder = Color(0xFFE0E5EB)

fun getCurrentHour(): String {
    val currentHour = LocalTime.now().hour
    return when (currentHour) {
        in 5..11 -> "morning"
        in 12..16 -> "afternoon"
        in 17..21 -> "evening"
        else      -> "night"
    }
}
@Composable
fun HomeScreen(brushingStatus: BrushingStatus,
               brushingTimeSeconds: Int,
               allSessions: List<BrushingSession>,
               onBrushNowClick: () -> Unit ) {
    Greeting(timeOfDay = getCurrentHour(),
        brushingStatus = brushingStatus,
        brushingTimeSeconds = brushingTimeSeconds,
        allSessions  = allSessions,
        onBrushNowClick = onBrushNowClick)
}

@Composable
fun SessionCard(session: BrushingSession) {
    // Format the timestamp (Long) into a readable date/time string
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(session.startTime))

    // Format duration into minutes and seconds (e.g., 2m 15s)
    val minutes = session.durationSeconds / 60
    val seconds = session.durationSeconds % 60
    val formattedDuration = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        // Rounded corners
        shape = RoundedCornerShape(20.dp),

        // white card background
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),

        // Light border around the card
        border = BorderStroke(1.dp, CardBorder),

        // Very small shadow
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )

    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row showing ID and Duration side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                // Session title
                Text(
                    text = "Brushing Session ${session.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingText
                )
//                Text(
//                    text = "ID: ${session.id}",
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.secondary
//                )
                Text(
                    text = "Duration: $formattedDuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryMint
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Start Time line
            Text(
                text = "Started: $formattedDate",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
        }
    }
}
@Composable
fun HistoryScreen(AllSessions: List<BrushingSession>) {
    // Main vertical layout for the entire History screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Space above the History icon
        Spacer(modifier = Modifier.height(16.dp))
        // Icon on the screen
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "History",
            tint = PrimaryMint,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterHorizontally)
        )
        // Space between the icon and title
        Spacer(modifier = Modifier.height(8.dp))
        // Screen title
        Text(
            text = "Brushing History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = HeadingText,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // Space below the title
        Spacer(modifier = Modifier.height(8.dp))

        // The total number of sessions saved in Room
        Text(
            text = "${AllSessions.size} completed sessions",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Space between the header and the session list
        Spacer(modifier = Modifier.height(24.dp))
        // Checking whether the database returned any sessions
        if (AllSessions.isEmpty()) {
            // Showing an empty-state message when there are no sessions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Occupying the remaining vertical space
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "No brushing sessions yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText
                )
            }

        } else {
            // Displaying the saved sessions in a vertically scrolling list
            LazyColumn(
                modifier = Modifier.weight(1f),

                // Adding space at the bottom of the list
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Creating one list item for each saved brushing session
                items(
                    items = AllSessions,

                    // Using the database ID as a stable unique key
                    key = { session -> session.id }
                ) { session ->

                    // Displaying the current session using SessionCard
                    SessionCard(session = session)
                }
            }
        }
    }
}
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = PrimaryMint
        )

        Text(
            text = "Profile",
            color = HeadingText
        )
    }
}

@Composable
fun DevicesScreen(
    devices: List<BluetoothDeviceModel>,
    scannedDevices: List<BluetoothDeviceModel>,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit,
    onDeviceSelected: (BluetoothDeviceModel) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Background bubbles
        BubbleBackground()

        // Decorative smiling tooth
        Image(
            painter = painterResource(id = R.drawable.smiley_tooth),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 55.dp, y = (-35).dp)
                .size(230.dp)
                .alpha(0.35f)
        )

        // Main screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Bluetooth icon
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                tint = HeadingText,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = "My Devices",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = HeadingText
            )

            Text(
                text = "Manage your Bluetooth toothbrush",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Paired Devices section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Paired Devices",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = HeadingText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Devices already paired with your phone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Original LazyColumn for paired devices
            if (devices.isEmpty()) {
                Text(
                    text = "No paired devices found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(devices) { device ->
                        DeviceItem(
                            device = device,
                            onDeviceSelected = onDeviceSelected
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scan button
            Button(
                onClick = onStartScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryMint,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Start Scanning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scanned Devices:",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = HeadingText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Keep filtering unknown or unnamed devices
            val visibleDevices = scannedDevices.filter {
                !it.name.isNullOrBlank() && it.name != "Unknown"
            }

            // Original LazyColumn for scanned devices
            if (visibleDevices.isEmpty()) {
                Text(
                    text = "No scanned devices found.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HeadingText
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(visibleDevices) { device ->
                        DeviceItem(
                            device = device,
                            onDeviceSelected = onDeviceSelected
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStopScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryMint,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Stop Scanning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
@Composable
fun DeviceItem(device: BluetoothDeviceModel,
               onDeviceSelected: (BluetoothDeviceModel) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                onDeviceSelected(device)
            },
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fallback to "Unknown Device" if the name is null
            Text(
                text = device.name ?: "Unknown Device",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = HeadingText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.address, // MAC Address
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
        }
    }
}


fun isMorningDoneFromDb(allSessions: List<BrushingSession>,
                        timeOfDay: String): Boolean {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val today = dateFormatter.format(Date())
    val hourRange = when (timeOfDay.lowercase())
    {
        "morning" -> 5..11
        "evening" -> 15..21
        else -> return false
    }
    return allSessions.any() { session ->
        val sessionDate = Date(session.startTime)
        val formattedDate = dateFormatter.format(sessionDate)
        val calendar = Calendar.getInstance()
        calendar.time = sessionDate
        val sessionHour = calendar.get(Calendar.HOUR_OF_DAY)
        formattedDate == today && sessionHour in hourRange
    }
}

@Composable
fun Greeting(timeOfDay: String,
             brushingStatus: BrushingStatus,
             brushingTimeSeconds: Int,
             allSessions: List<BrushingSession>,
             onBrushNowClick: () -> Unit = {},
             modifier: Modifier = Modifier) {
    // the state variable at the top of Composable

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3FCF9))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Good $timeOfDay!",
                color = Color(0xFF191B2C),
                fontSize = 25.sp
            )

            Image(
                painter = painterResource(id = R.drawable.smiley_tooth),
                contentDescription = "Smiley Tooth Face",
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Let's keep your smile healthy",
            color = Color(0xFF191B2C),
            fontSize = 18.sp )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            // Customize corner roundness (e.g., 12.dp)
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White
            ))
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            {
                Text(text = "Today's goal",
                    color = Color(0xFF191B2C),
                    fontSize = 20.sp )
                Text(text = "Brush at least 2 times",
                    color = Color(0xFF191B2C),
                    fontSize = 16.sp )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Morning",
                            color = Color(0xFF596170),
                            fontSize = 16.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMorningDoneFromDb(allSessions,
                                        "Morning"))Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isMorningDoneFromDb(allSessions, "Morning"))
                                    Color(0xFF00C9A7)
                                else
                                    Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = if (isMorningDoneFromDb(allSessions,
                                        "Morning"))"Done!" else "Not yet",
//                                text = "Done",
                                color = Color(0xFF596170),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Evening",
                            color = Color(0xFF596170),
                            fontSize = 16.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMorningDoneFromDb(allSessions,
                                        "Evening"))Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isMorningDoneFromDb(allSessions, "Evening"))
                                    Color(0xFF00C9A7)
                                else
                                    Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = if (isMorningDoneFromDb(allSessions,
                                        "Evening"))"Done!" else "Not yet",
                                color = Color(0xFF596170),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp)) // Space between two cards
        BrushNowButton(
            onClick = onBrushNowClick
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Weekly streak card

            WeeklyStreakCard(allSessions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = "Star Icon",
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Total brushes: ${allSessions.size}",
                    color = Color(0xFF191B2C),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            // Customize corner roundness (e.g., 12.dp)
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White
            ))
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "Heart",
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "A cleaner smile brighter days!",
                        color = Color(0xFF191B2C),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BrushBuddyTheme {
        Greeting("morning",
            brushingStatus = BrushingStatus.IDLE,
            allSessions = emptyList<BrushingSession>() ,
            brushingTimeSeconds = 0,
            onBrushNowClick = {})
    }
}