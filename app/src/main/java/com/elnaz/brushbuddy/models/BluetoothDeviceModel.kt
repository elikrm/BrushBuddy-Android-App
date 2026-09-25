package com.elnaz.brushbuddy.models
//represents a discovered Bluetooth device
data class BluetoothDeviceModel(
    //represents a discovered Bluetooth device.
    val name: String?,
    val address: String,
    val rssi: Int,
)
