package com.elnaz.brushbuddy.data.bluetooth
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import com.elnaz.brushbuddy.models.BluetoothState
import com.elnaz.brushbuddy.models.BrushingStatus
import com.elnaz.brushbuddy.models.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/*
 * Flow: represents a stream of values over time, similar to the Observer pattern / IObservable in C#.
 * StateFlow: a Flow that also holds the current/latest state.
 * suspend: marks a function that can perform asynchronous work without blocking the thread.
 */

interface BluetoothRepository {
    val bluetoothState: StateFlow<BluetoothState>
    val brushingStatus: StateFlow<BrushingStatus>
    val bondedDevices: StateFlow<List<BluetoothDeviceModel>>
    fun startScanning(): Flow<BluetoothDeviceModel>
    fun stopScanning()
    fun connectToDevice(device: BluetoothDeviceModel): Flow<ConnectionState>
    suspend fun disconnect()
    fun loadMyPairedDevices()
}
