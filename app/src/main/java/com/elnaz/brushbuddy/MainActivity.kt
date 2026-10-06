package com.elnaz.brushbuddy

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.elnaz.brushbuddy.ui.theme.BrushBuddyTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Needed for .sp font sizes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource // NEEDED for local drawables
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elnaz.brushbuddy.ui.components.HomeScreen
import com.elnaz.brushbuddy.ui.components.ProfileScreen
import com.elnaz.brushbuddy.ui.components.HistoryScreen
import com.elnaz.brushbuddy.utils.Constants
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elnaz.brushbuddy.data.bluetooth.AndroidBluetoothRepository
import com.elnaz.brushbuddy.data.bluetooth.BluetoothRepository
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import com.elnaz.brushbuddy.models.BluetoothViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

import com.elnaz.brushbuddy.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    private var scanJob: Job? = null
    private lateinit var bluetoothViewModel: BluetoothViewModel
    private lateinit var bluetoothRepository: AndroidBluetoothRepository
    @android.annotation.SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initializing Repository and ViewModel
        bluetoothRepository = AndroidBluetoothRepository(applicationContext)
        bluetoothViewModel = BluetoothViewModel(bluetoothRepository)
        // setting up permission launcher
        val requiredPermissions: Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        // Registering the permission request launcher
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT) { permissions ->
            // Check if all requested permissions were granted by the user
            val allGranted = permissions.entries.all { it.value }

            if (allGranted) {
                Log.d("BrushBuddy", "All permissions granted! Ready to scan and track proximity.")
                // TODO: Trigger startScanning() logic here
                bluetoothViewModel.onPermissionsGranted()
            } else {
                Log.d("BrushBuddy", "Permissions denied. Cannot discover the toothbrush.")
                // TODO: Update repository state to BluetoothState.UNAUTHORIZED here
                bluetoothViewModel.onPermissionsDenied()
            }
        }
        // 3. Asking the user for the permissions
        requestPermissionLauncher.launch(requiredPermissions)
        enableEdgeToEdge()

        setContent {
            val deviceList by bluetoothRepository.bondedDevices.collectAsStateWithLifecycle()
            val scannedDeviceList by bluetoothRepository.scannedDevices.collectAsStateWithLifecycle()


            BrushBuddyTheme {
                AppNavHost(

                    deviceList = deviceList,
                    scannedDeviceList = scannedDeviceList,
                    onStartScanning = {
                        scanJob?.cancel()
                        scanJob = lifecycleScope.launch @androidx.annotation.RequiresPermission(
                            allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT]
                        ) {
                            bluetoothRepository.startScanning().collect { device ->
                                Log.d("BrushBuddy", "Device: ${device.name}")
                            }
                        }
                    },
                    onStopScanning = {
                        scanJob?.cancel()
                        scanJob = null
                    },
                    onDeviceSelected = { device ->
                        lifecycleScope.launch @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT) {
                            bluetoothRepository.connectToDevice(device).collect { state ->
                                Log.d("BrushBuddy", "Connection state: $state")
                            }
                        }
                    }
                )
            }

        }
    }
}
