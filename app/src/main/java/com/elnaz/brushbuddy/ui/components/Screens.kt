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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.elnaz.brushbuddy.R

@Composable
fun HomeScreen(brushingStatus: BrushingStatus, brushingTimeSeconds: Int) {
    Greeting(name = "Morning",
        brushingStatus = brushingStatus,
        brushingTimeSeconds = brushingTimeSeconds)
}

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon on the screen
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "history",
            tint = Color(0xFF0F9D58)
        )
        // Text on the screen
        Text(text = "History", color = Color.Black)
    }
}
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = Color(0xFF0F9D58)
        )

        Text(
            text = "Profile",
            color = Color.Black
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
            .background(Color(0xFFFFE0E9))
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
                tint = Color(0xFF7D2855),
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = "My Devices",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7D2855)
            )

            Text(
                text = "Manage your Bluetooth toothbrush",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF75656D)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Paired Devices section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF0F5)
                ),
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
                        color = Color(0xFF7D2855)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Devices already paired with your phone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF75656D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Original LazyColumn for paired devices
            if (devices.isEmpty()) {
                Text(
                    text = "No paired devices found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF75656D)
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
                    containerColor = Color(0xFF64A5F7),
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
                color = Color(0xFF7D2855)
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
                    color = Color(0xFF7D2855)
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
                    containerColor = Color(0xFF64A5F7),
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
            containerColor = Color(0xFFFFF5F8)
        ),
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
                color = Color(0xFF7D2855)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.address, // MAC Address
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF75656D)
            )
        }
    }
}