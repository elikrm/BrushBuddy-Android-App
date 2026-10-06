package com.elnaz.brushbuddy.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import com.elnaz.brushbuddy.ui.components.LogiScreen
import com.elnaz.brushbuddy.ui.components.MainScreen
import com.elnaz.brushbuddy.ui.components.Signup



    @SuppressLint("NotConstructor")
    @Composable
    fun AppNavHost(
        navController: NavHostController = rememberNavController(),
        startDestination: String = "MainScreen", // Set to "login" when ready to enable login
        deviceList: List<BluetoothDeviceModel>,
        scannedDeviceList: List<BluetoothDeviceModel>,
        onStartScanning: () -> Unit,
        onStopScanning: () -> Unit,
        onDeviceSelected: (BluetoothDeviceModel) -> Unit
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination// Starts directly on MainScreen

        ) {
            composable("signup") {
                Signup()
            }
            composable (route = "login"){
                LogiScreen()
            }
            composable("MainScreen") {
                MainScreen(
                    deviceList = deviceList,
                    scannedDeviceList = scannedDeviceList,
                    onStartScanning = onStartScanning,
                    onStopScanning = onStopScanning,
                    onDeviceSelected = onDeviceSelected
                )
            }
        }


    }