package com.elnaz.brushbuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
//import com.elnaz.brushbuddy.Greeting
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elnaz.brushbuddy.models.MainViewModel

@Composable
fun HomeScreen(  viewModel: MainViewModel = viewModel()) {
    val brushingState by viewModel.brushingState.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()

    Greeting(
        name = "Morning",
        brushingState = brushingState,
        elapsedSeconds= elapsedSeconds,
        messageToUser = viewModel.messageToUser,
        ({viewModel.onHeartClicked()})
    )
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
fun ProfileScreen(devices: List<BluetoothDeviceModel>,
                  scannedDevices: List<BluetoothDeviceModel>,
                  onStartScanning: () -> Unit,
                  onStopScanning: () -> Unit,
                  onDeviceSelected: (BluetoothDeviceModel) -> Unit){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon on the screen
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = Color(0xFF0F9D58)
        )
        // Text on the screen
        Text(text = "Profile", color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Paired Devices",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if(devices.isEmpty())
        {
            Text(
                text = "No paired devices found.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        else
        {
            LazyColumn(
                modifier = Modifier.weight(1f)
            )
            {
                items(devices){
                    device ->
                    DeviceItem(device =device,
                        onDeviceSelected = onDeviceSelected)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStartScanning
        ) {
            Text("Start Scanning")
        }
        Text(
            text = "Scanned Devices:",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if(scannedDevices.isEmpty())
        {
            Text(
                text = "No scanned devices found.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        else
        {
            LazyColumn(
                modifier = Modifier.weight(1f)
            )
            {
                items(scannedDevices){
                        device ->
                    DeviceItem(device =device,
                        onDeviceSelected = onDeviceSelected)
                }
            }
        }
        Button(
            onClick = onStopScanning
        ) {
            Text("Stop Scanning")
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
            .clickable{
                onDeviceSelected(device)
                      },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fallback to "Unknown Device" if the name is null
            Text(
                text = device.name ?: "Unknown Device",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.address, // MAC Address
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}