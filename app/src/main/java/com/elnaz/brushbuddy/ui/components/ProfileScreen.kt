package com.elnaz.brushbuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
@Composable
fun ProfileScreen(
    userEmail: String = "user@example.com",
    devices: List<BluetoothDeviceModel>,
    scannedDevices: List<BluetoothDeviceModel>,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit,
    onDeviceSelected: (BluetoothDeviceModel) -> Unit,
    onSignOutClicked: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. User Account Info ---
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = Color(0xFF0F9D58),
            modifier = Modifier.size(64.dp)
        )
        Text(text = userEmail, style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Bluetooth Management ---
        Text(text = "Paired Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        // ... (Your existing LazyColumn for devices & Scan buttons)

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. Sign Out Button ---
        Button(
            onClick = onSignOutClicked,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Sign Out", color = Color.White)
        }
    }
}