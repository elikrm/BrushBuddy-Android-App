package com.elnaz.brushbuddy.models

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elnaz.brushbuddy.data.bluetooth.AndroidBluetoothRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.jar.Manifest


class BluetoothViewModel(private val bluetoothRepository: AndroidBluetoothRepository): ViewModel(){
    val deviceList = bluetoothRepository.bondedDevices
    val scannedDeviceList = bluetoothRepository.scannedDevices
    private var scanJob : Job? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun startScanning(){
        scanJob?.cancel()
        scanJob = viewModelScope.launch{

            bluetoothRepository.startScanning().collect { device ->
                Log.d("BrushBuddy", "Found device: ${device.name}")
            }
        }

    }
    @SuppressLint("MissingPermission")
    fun onPermissionsGranted(){
        Log.d("BrushBuddy","Permissions granted! Loading paired devices")
        bluetoothRepository.loadMyPairedDevices()
    }
    fun onPermissionsDenied() {
        Log.d("BrushBuddy", "Permissions denied. Cannot discover the toothbrush.")
        // TODO: Update repository state to BluetoothState.UNAUTHORIZED here
    }
    fun onDeviceSelected(device: BluetoothDeviceModel) {
        Log.d("BrushBuddy", "Selected device: ${device.name}, Address: ${device.address}")
    }



}