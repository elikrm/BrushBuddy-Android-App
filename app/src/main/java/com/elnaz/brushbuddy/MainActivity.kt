package com.elnaz.brushbuddy

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elnaz.brushbuddy.ui.components.HomeScreen
import com.elnaz.brushbuddy.ui.components.ProfileScreen
import com.elnaz.brushbuddy.ui.components.HistoryScreen
import com.elnaz.brushbuddy.utils.Constants
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        ) { permissions ->
            // Check if all requested permissions were granted by the user
            val allGranted = permissions.entries.all { it.value }

            if (allGranted) {
                Log.d("BrushBuddy", "All permissions granted! Ready to scan and track proximity.")
                // TODO: Trigger startScanning() logic here
            } else {
                Log.d("BrushBuddy", "Permissions denied. Cannot discover the toothbrush.")
                // TODO: Update repository state to BluetoothState.UNAUTHORIZED here
            }
        }
        // 3. Asking the user for the permissions
        requestPermissionLauncher.launch(requiredPermissions)
        enableEdgeToEdge()
        setContent {
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
                        padding = innerPadding
                    )
                }
            }
        }
    }
}
//follow this https://www.geeksforgeeks.org/kotlin/bottom-navigation-bar-in-android-jetpack-compose/
@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(padding)
    ) {
        composable("home") {
            HomeScreen()
        }

        composable("history") {
            HistoryScreen()
        }

        composable("profile") {
            ProfileScreen()
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController) {

    NavigationBar(
        // set background color
        containerColor = Color.White,
        modifier = Modifier.height(64.dp)
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
                    navController.navigate(navItem.route)
                },
                // Icon of navItem
                icon = {
                    Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                },
                // label
                label = {
                    Text(text = navItem.label)
                },
                alwaysShowLabel = false,
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
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
            .clickable { /* Action */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text("Good $name!",
                color = Color.Blue,
                fontSize = 25.sp  )
            Spacer(modifier = Modifier.width(30.dp)) // Space between row elements
            Image(
                painter = painterResource(id = R.drawable.smiley_tooth),
                contentDescription = "Smiley Tooth Face",
                modifier = Modifier.size(150.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Let's keep your smile healthy",
            color = Color.Blue,
            fontSize = 25.sp )
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
                    .padding(24.dp)
            )
            {
                Text(text = "Today's goal",
                    color = Color.Blue,
                    fontSize = 25.sp )
                Text(text = "Brush at least 2 times",
                    color = Color.Blue,
                    fontSize = 25.sp )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Morning\nDone!",
                        color = Color.DarkGray,
                        fontSize = 18.sp,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Space between row elements
                    // Second item inside the row
                    Text(
                        text = "Evening\nNot yet",
                        color = Color.Black,
                        fontSize = 18.sp
                    )
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
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.brush),
                        contentDescription = "Brush Image",
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Space between row elements
                    Text(
                        text = " $brushingState Time: $elapsedSeconds s",
                        color = Color.DarkGray,
                        fontSize = 18.sp,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Progress Bar (elapsed / max seconds), capped at 1.0 (100%)
                val progress = (elapsedSeconds / 30f).coerceAtMost(1.0f)
                LinearProgressIndicator(
                    progress = { progress }, // 0.5f represents 50% progress (1 out of 2 completed)
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50), // Green filled track for success
                    trackColor = Color(0xFFE0E0E0), // Light gray background track
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$messageToUser.",
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
                    .padding(24.dp)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Card(
                        modifier = Modifier.weight(1f),
                        // Customize corner roundness (e.g., 12.dp)
                        shape = RoundedCornerShape(12.dp),
                        // Set the card background to white so it stands out against the pink
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC0CB))
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.streak),
                            contentDescription = "Streak Image",
                            modifier = Modifier.size(50.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "Day streak",
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp)) // Space between two cards
                    Card(
                        modifier = Modifier.weight(1f),
                        // Customize corner roundness (e.g., 12.dp)
                        shape = RoundedCornerShape(12.dp),
                        // Set the card background to white so it stands out against the pink
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC0CB))
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Star Icon",
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "Total brushes",
                            color = Color.Black,
                            fontSize = 18.sp
                        )
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
                    .padding(24.dp)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "Heart",
                        modifier = Modifier
                            .size(50.dp)
                            .clickable
                            {
                                /*
                                * NOT_BRUSHING       → BRUSHING
                                * BRUSHING           → check elapsed time
                                * ABORTED_SESSION    → NOT_BRUSHING
                                * CONFIRMED_SESSION  → NOT_BRUSHING */
                                brushingState = when(brushingState)
                                {
                                    BrushingStateEnum.NOT_BRUSHING -> {BrushingStateEnum.BRUSHING}
                                    BrushingStateEnum.BRUSHING ->
                                    {
                                    if (elapsedSeconds < 10) { BrushingStateEnum.ABORTED_SESSION }
                                    else { BrushingStateEnum.CONFIRMED_SESSION } }
                                    BrushingStateEnum.ABORTED_SESSION -> { BrushingStateEnum.ABORTED_SESSION }

                                    BrushingStateEnum.CONFIRMED_SESSION -> { BrushingStateEnum.CONFIRMED_SESSION }
                                }
                            }
                    )
                    Text(
                        text = " A cleaner smile brighter days!",
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
        Greeting("Morning")
    }
}