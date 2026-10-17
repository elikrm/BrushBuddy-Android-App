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
import com.elnaz.brushbuddy.Greeting
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import com.elnaz.brushbuddy.models.BrushingStatus
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.elnaz.brushbuddy.R
import com.elnaz.brushbuddy.data.local.BrushingSession
import java.text.SimpleDateFormat
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

@Composable
fun HomeScreen(brushingStatus: BrushingStatus,
               brushingTimeSeconds: Int,
               totalBrushes: Int,
               onBrushNowClick: () -> Unit ) {
    Greeting(name = "Morning",
        brushingStatus = brushingStatus,
        brushingTimeSeconds = brushingTimeSeconds,
        totalBrushes  = totalBrushes,
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
