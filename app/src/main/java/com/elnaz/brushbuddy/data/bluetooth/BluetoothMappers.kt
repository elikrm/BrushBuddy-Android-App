package com.elnaz.brushbuddy.data.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
//BluetoothDevice.getName() requires android.permission.BLUETOOTH_CONNECT
//On newer Android versions, reading properties such as the remote device's name requires Bluetooth permission
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothDevice.toBluetoothDeviceModel(rssi: Int = 0): BluetoothDeviceModel {
    return BluetoothDeviceModel(
        name = this.name, // Safely handles null names in Kotlin
        address = this.address,
        rssi = rssi
    )
}