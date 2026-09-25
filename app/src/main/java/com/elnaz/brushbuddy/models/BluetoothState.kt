package com.elnaz.brushbuddy.models
//represents the phone Bluetooth state
//sealed because we want a fixed set of Bluetooth states.
sealed interface BluetoothState {
    //represents the state of the phone's Bluetooth system.
    object Enabled : BluetoothState //Bluetooth ready
    object Off : BluetoothState //Bluetooth turned off
    object Unauthorized : BluetoothState // Permissions missing
}