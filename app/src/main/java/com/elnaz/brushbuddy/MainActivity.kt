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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.elnaz.brushbuddy.ui.components.WeeklyStreakCard
import com.elnaz.brushbuddy.ui.screens.BrushingScreen
import com.elnaz.brushbuddy.ui.components.BrushNowButton
import com.elnaz.brushbuddy.models.BrushingStateEnum
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elnaz.brushbuddy.data.local.BrushBuddyDatabase
import com.elnaz.brushbuddy.data.local.BrushingSessionRepository
import com.elnaz.brushbuddy.ui.viewmodel.BrushingViewModel

class MainActivity : ComponentActivity() {
    private var scanJob: Job? = null
    private lateinit var bluetoothRepository: AndroidBluetoothRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothRepository = AndroidBluetoothRepository(this)
        // Initialise Room and the ViewModel for saving brushing sessions
        val database = BrushBuddyDatabase.getDatabase(applicationContext)
        val repository = BrushingSessionRepository(database.brushingSessionDao())
        lifecycleScope.launch {
            repository.allSessions.collect { sessions ->
                Log.d("BrushBuddy", "Saved sessions: ${sessions.size}")
            }
        }
        val brushingViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BrushingViewModel(repository, bluetoothRepository) as T
                }
            }
        )[BrushingViewModel::class.java]
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
            val brushingState by bluetoothRepository.brushingState.collectAsState()
            val totalBrushes by brushingViewModel.totalBrushes.collectAsStateWithLifecycle()
            BrushBuddyTheme {
                val navController = rememberNavController()
                var previousBrushingStatus by remember {
                    mutableStateOf(brushingStatus)
                }
                LaunchedEffect(brushingStatus) {
                    if (
                        previousBrushingStatus != BrushingStatus.BRUSHING &&
                        brushingStatus == BrushingStatus.BRUSHING
                    ) {
                        navController.navigate("brushing") {
                            launchSingleTop = true
                        }
                    }

                    previousBrushingStatus = brushingStatus
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                )
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
                        brushingState,
                        totalBrushes = totalBrushes
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
    brushingState: BrushingStateEnum,
    totalBrushes: Int
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(padding)
    ) {
        composable("home") {
            HomeScreen(
                brushingStatus = brushingStatus,
                brushingTimeSeconds = brushingTimeSeconds,
                totalBrushes = totalBrushes,
                onBrushNowClick = {
                    navController.navigate("brushing")
                }
            )
        }
        composable("brushing") {
            BrushingScreen(
                brushingStatus = brushingStatus,
                brushingTimeSeconds = brushingTimeSeconds,
                brushingState = brushingState
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
                            inclusive = false
                        }
                        launchSingleTop = true
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
                    selectedIconColor = Color(0xFF00C9A7),
                    unselectedIconColor = Color(0xFF596170),
                    selectedTextColor = Color(0xFF00C9A7),
                    indicatorColor = Color(0xFFD9F8EF)
                )
            )
        }
    }
}

@Composable
fun Greeting(name: String,
             brushingStatus: BrushingStatus,
             brushingTimeSeconds: Int,
             totalBrushes : Int,
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
                text = "Good $name!",
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00C9A7),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Done!",
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
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Not yet",
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
            WeeklyStreakCard()
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
                    text = "Total brushes: $totalBrushes",
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
        Greeting("Morning",
            brushingStatus = BrushingStatus.IDLE,
            totalBrushes = 0,
            brushingTimeSeconds = 0,
            onBrushNowClick = {})
    }
}
