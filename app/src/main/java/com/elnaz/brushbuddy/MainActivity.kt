package com.elnaz.brushbuddy

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elnaz.brushbuddy.data.bluetooth.AndroidBluetoothRepository
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import com.elnaz.brushbuddy.models.BrushingStatus
import com.elnaz.brushbuddy.ui.components.DevicesScreen
import com.elnaz.brushbuddy.ui.components.HistoryScreen
import com.elnaz.brushbuddy.ui.components.HomeScreen
import com.elnaz.brushbuddy.ui.components.ProfileScreen
import com.elnaz.brushbuddy.ui.theme.BrushBuddyTheme
import com.elnaz.brushbuddy.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var scanJob: Job? = null
    private lateinit var bluetoothRepository: AndroidBluetoothRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothRepository = AndroidBluetoothRepository(this)
        // 1. The array of permissions based on the Android system version
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
        // 2. Registering the permission request launcher
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT) { permissions ->
            // Check if all requested permissions were granted by the user
            val allGranted = permissions.entries.all { it.value }

            if (allGranted) {
                Log.d("BrushBuddy", "All permissions granted! Ready to scan and track proximity.")
                // TODO: Trigger startScanning() logic here
                bluetoothRepository.loadMyPairedDevices()
            } else {
                Log.d("BrushBuddy", "Permissions denied. Cannot discover the toothbrush.")
                // TODO: Update repository state to BluetoothState.UNAUTHORIZED here
            }
        }
        // 3. Asking the user for the permissions
        requestPermissionLauncher.launch(requiredPermissions)
        enableEdgeToEdge()
        setContent {
            val deviceList by bluetoothRepository.bondedDevices.collectAsStateWithLifecycle()
            val scannedDeviceList by bluetoothRepository.scannedDevices.collectAsStateWithLifecycle()
            val brushingStatus by bluetoothRepository.brushingStatus.collectAsStateWithLifecycle()
            val brushingTimeSeconds by bluetoothRepository.brushingTimeSeconds.collectAsStateWithLifecycle()
            BrushBuddyTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                )
//                { innerPadding ->
//                    Greeting(
//                        name = "Morning",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
                { innerPadding ->
                    NavHostContainer(
                        navController = navController,
                        padding = innerPadding,
                        deviceList,
                        scannedDeviceList,
                        onStartScanning = {
                            scanJob?.cancel()

                            scanJob = lifecycleScope.launch {

                                bluetoothRepository.startScanning().collect { device ->

                                    Log.d(
                                        "BrushBuddy",
                                        "Device: ${device.name}, " +
                                                "Address: ${device.address}, " +
                                                "RSSI: ${device.rssi}"
                                    )
                                }
                            }
                        },
//                        onStartScanning = @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT]) {
//                            bluetoothRepository.testSimpleScan()
//                        },
                        onStopScanning = {
                            Log.d("BrushBuddy", "Stop scanning requested")

                            scanJob?.cancel()
                            scanJob = null
                        },
                        onDeviceSelected = { device ->
                            //just logging for now
                            Log.d(
                                "BrushBuddy",
                                "Selected device: ${device.name}, Address: ${device.address}"
                            )
                            lifecycleScope.launch {
                                bluetoothRepository.connectToDevice(device).collect { connectionState ->
                                    Log.d(
                                        "BrushBuddy",
                                        "Connection state: $connectionState"
                                    )
                                }
                            }
                        },
                        brushingStatus,
                        brushingTimeSeconds,
                    )
                }
            }
        }
    }
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues,
    deviceList: List<BluetoothDeviceModel>,
    scannedDeviceList : List<BluetoothDeviceModel>,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit,
    onDeviceSelected: (BluetoothDeviceModel) -> Unit,
    brushingStatus: BrushingStatus,
    brushingTimeSeconds: Int,
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(padding)
    ) {
        composable("home") {
            HomeScreen(
                brushingStatus = brushingStatus,
                brushingTimeSeconds = brushingTimeSeconds
            )
        }

        composable("history") {
            HistoryScreen()
        }

        composable("devices") {
            DevicesScreen(deviceList,
                scannedDeviceList,
                onStartScanning,
                onStopScanning,
                onDeviceSelected)
        }
        // Profile screen: user settings and preferences
        composable("profile") {
            ProfileScreen()
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController) {

    NavigationBar(
        // set background color
        containerColor = Color.White
    ) {
        // observe the backstack
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        // observe current route to change the icon
        // color,label color when navigated
        val currentRoute = navBackStackEntry?.destination?.route

        // Bottom nav items we declared
        Constants.BottomNavItems.forEach { navItem ->
            // Place the bottom nav items
            NavigationBarItem(
                // it currentRoute is equal then its selected route
                selected = currentRoute == navItem.route,
                // navigate on click
                onClick = {
                    // Previous navigation behaved like a stack (LIFO).
                    // Each navigate() call pushed a new screen onto the back stack,
                    // even if that screen was already visited.
                    // This could create duplicate screens when switching tabs.
                    //navController.navigate(navItem.route)
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                // Icon of navItem
                icon = {
                    Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                },
                // label
                label = {
                    Text(text = navItem.label)
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF0066FF), // Icon color when selected
                    unselectedIconColor = Color.Gray, // Icon color when not selected
                    selectedTextColor = Color(0xFF0066FF), // Label color when selected
                    indicatorColor = Color(0xFFFFD9DF) // Highlight color for selected item
                )
            )
        }
    }
}
enum class BrushingStateEnum {
    NOT_BRUSHING,
    BRUSHING,
    ABORTED_SESSION,
    CONFIRMED_SESSION,
}
@Composable
fun Greeting(name: String,
             brushingStatus: BrushingStatus,
             brushingTimeSeconds: Int,
             modifier: Modifier = Modifier) {
    // the state variable at the top of Composable
    var brushingState by remember {
        mutableStateOf(BrushingStateEnum.NOT_BRUSHING)
    }
    val messageToUser = remember(brushingState)
    {
        when (brushingState) {
            BrushingStateEnum.NOT_BRUSHING -> {"Ready to brush"}
            BrushingStateEnum.BRUSHING ->{"Keep going! You need at least 30 seconds"}
            BrushingStateEnum.CONFIRMED_SESSION -> {"Great job!"}
            BrushingStateEnum.ABORTED_SESSION -> {"Session too short"}
        }
    }
    var elapsedSeconds by remember {
        mutableStateOf(0)
    }
    fun heartPress() {
        /*
        * NOT_BRUSHING       → BRUSHING
        * BRUSHING           → check elapsed time
        * ABORTED_SESSION    → NOT_BRUSHING
        * CONFIRMED_SESSION  → NOT_BRUSHING */
        brushingState = when(brushingState) {
            BrushingStateEnum.NOT_BRUSHING -> {BrushingStateEnum.BRUSHING}
            BrushingStateEnum.BRUSHING -> {
                if (elapsedSeconds < 10) { BrushingStateEnum.ABORTED_SESSION }
                else { BrushingStateEnum.CONFIRMED_SESSION } }
            BrushingStateEnum.ABORTED_SESSION -> { BrushingStateEnum.ABORTED_SESSION }
            BrushingStateEnum.CONFIRMED_SESSION -> { BrushingStateEnum.CONFIRMED_SESSION }
        }
    }
    LaunchedEffect(brushingState) {
        when (brushingState) {

            BrushingStateEnum.BRUSHING -> {
                elapsedSeconds = 0

                while (true) {
                    delay(1000)
                    elapsedSeconds++
                }
            }

            BrushingStateEnum.ABORTED_SESSION,
            BrushingStateEnum.CONFIRMED_SESSION -> {
                delay(1000)
                brushingState = BrushingStateEnum.NOT_BRUSHING
            }

            BrushingStateEnum.NOT_BRUSHING -> {
                // Nothing to do
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFD9DF)) // Capital 'P' for Pink
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Good $name!",
                color = Color.Blue,
                fontSize = 25.sp
            )

            Image(
                painter = painterResource(id = R.drawable.smiley_tooth),
                contentDescription = "Smiley Tooth Face",
                modifier = Modifier.size(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Let's keep your smile healthy",
            color = Color.Blue,
            fontSize = 18.sp )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            // Customize corner roundness (e.g., 12.dp)
            shape = RoundedCornerShape(12.dp),
            // Set the card background to white so it stands out against the pink
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC0CB))
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            {
                Text(text = "Today's goal",
                    color = Color.Blue,
                    fontSize = 20.sp )
                Text(text = "Brush at least 2 times",
                    color = Color.Blue,
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
                            color = Color.DarkGray,
                            fontSize = 16.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Done!",
                                color = Color.DarkGray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Evening",
                            color = Color.DarkGray,
                            fontSize = 16.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Not yet",
                                color = Color.DarkGray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Space between two cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            // Customize corner roundness (e.g., 12.dp)
            shape = RoundedCornerShape(12.dp),
            // Set the card background to white so it stands out against the pink
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC0CB))
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.brush),
                        contentDescription = "Brush Image",
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (brushingStatus) {
                                BrushingStatus.BRUSHING -> "Brushing in progress"
                                else -> "Ready to brush"
                            },
                            color = Color.DarkGray,
                            fontSize = 18.sp
                        )

                        val minutes = brushingTimeSeconds / 60
                        val seconds = brushingTimeSeconds % 60

                        Text(
                            text = "%02d:%02d".format(minutes, seconds),
                            color = Color.DarkGray,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress towards the minimum 30-second brushing session
                val progress = (brushingTimeSeconds / 30f).coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message based on real Bluetooth brushing status
                val messageToUser = when {
                    brushingStatus == BrushingStatus.BRUSHING &&
                            brushingTimeSeconds >= 30 ->
                        "Great job! Keep brushing!"

                    brushingStatus == BrushingStatus.BRUSHING ->
                        "Keep going!"

                    else ->
                        "Ready to brush"
                }

                Text(
                    text = messageToUser,
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            // Customize corner roundness (e.g., 12.dp)
            shape = RoundedCornerShape(12.dp),
            // Set the card background to white so it stands out against the pink
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC0CB))
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFC0CB)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.streak),
                                contentDescription = "Streak Image",
                                modifier = Modifier.size(50.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Day streak",
                                color = Color.Black,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFC0CB)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.star),
                                contentDescription = "Star Icon",
                                modifier = Modifier.size(50.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Total brushes",
                                color = Color.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            // Customize corner roundness (e.g., 12.dp)
            shape = RoundedCornerShape(12.dp),
            // Set the card background to white so it stands out against the pink
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC0CB))
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                        color = Color.Black,
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
        Greeting("Morning",
            brushingStatus = BrushingStatus.IDLE,
            brushingTimeSeconds = 0)
    }
}