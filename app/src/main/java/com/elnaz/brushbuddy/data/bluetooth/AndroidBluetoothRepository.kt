package com.elnaz.brushbuddy.data.bluetooth
import android.Manifest
import android.content.Context
import com.elnaz.brushbuddy.models.BluetoothState
import kotlinx.coroutines.flow.MutableStateFlow
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import com.elnaz.brushbuddy.models.BrushingStatus
import com.elnaz.brushbuddy.models.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


//MutableStateFlow -> repository can change the value
//StateFlow -> other classes can observe it but shouldn't change it
class AndroidBluetoothRepository(private val context: Context
) : BluetoothRepository {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    //mutable internally
    private val _bluetoothState = MutableStateFlow(
        if (bluetoothAdapter?.isEnabled == true) BluetoothState.Enabled
        else BluetoothState.Off
    )
    //read-only externally
    override val bluetoothState: StateFlow<BluetoothState> = _bluetoothState.asStateFlow()
    //mutable internally
    private val _bondedDevices =
        MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    //read-only externally
    override val bondedDevices: StateFlow<List<BluetoothDeviceModel>> =
        _bondedDevices.asStateFlow()
    //mutable internally
    private val _brushingStatus =
        MutableStateFlow(BrushingStatus.IDLE)
    //read-only externally
    override val brushingStatus: StateFlow<BrushingStatus> =
        _brushingStatus.asStateFlow()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun loadMyPairedDevices(){
        val devices = bluetoothAdapter.bondedDevices.map { bluetoothDevice ->
            bluetoothDevice.toBluetoothDeviceModel()
        }

        _bondedDevices.value = devices
    }
    override fun startScanning(): Flow<BluetoothDeviceModel> {
        TODO("Not yet implemented")
    }

    override fun stopScanning() {
        TODO("Not yet implemented")
    }

    override fun connectToDevice(device: BluetoothDeviceModel): Flow<ConnectionState> {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }

}