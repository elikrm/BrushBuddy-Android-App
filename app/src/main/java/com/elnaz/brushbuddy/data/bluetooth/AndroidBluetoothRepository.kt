package com.elnaz.brushbuddy.data.bluetooth
import android.Manifest
import android.content.Context
import com.elnaz.brushbuddy.models.BluetoothState
import kotlinx.coroutines.flow.MutableStateFlow
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import com.elnaz.brushbuddy.models.BrushingStatus
import com.elnaz.brushbuddy.models.ConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow


//MutableStateFlow -> repository can change the value
//StateFlow -> other classes can observe it but shouldn't change it
class AndroidBluetoothRepository(private val context: Context
) : BluetoothRepository {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
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
    private val _scannedDevices =
        MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDeviceModel>> =
        _scannedDevices.asStateFlow()
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
        Log.d("BrushBuddy", "Paired devices found: ${devices.size}")
        for (item in devices) {
            "Paired devices Name: ${item.name}, Address: ${item.address}, RSSI: ${item.rssi}"
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(
        allOf = [
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ]
    )
    override fun startScanning(): Flow<BluetoothDeviceModel> = callbackFlow {

        val scanCallback = object : ScanCallback() {

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {
                Log.d("BrushBuddy", "RAW onScanResult received")
                val bluetoothDeviceModel =
                    result.device.toBluetoothDeviceModel(
                        rssi = result.rssi
                    )

                Log.d(
                    "BrushBuddy",
                    "BLE scan result - " +
                            "Name: ${bluetoothDeviceModel.name}, " +
                            "Address: ${bluetoothDeviceModel.address}, " +
                            "RSSI: ${bluetoothDeviceModel.rssi}"
                )

                // Send each discovered device to the Flow
                trySend(bluetoothDeviceModel)

                // Add device only if we haven't already discovered it
                if (_scannedDevices.value.none { device ->
                        device.address == bluetoothDeviceModel.address
                    }) {

                    _scannedDevices.value += bluetoothDeviceModel
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(
                    "BrushBuddy",
                    "BLE scan failed: $errorCode"
                )

                close()
            }
        }

        val scanPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        )

        Log.d(
            "BrushBuddy",
            "BLUETOOTH_SCAN granted = " +
                    "${scanPermission == PackageManager.PERMISSION_GRANTED}"
        )

        Log.d("BrushBuddy", "Starting BLE scan")

        bluetoothLeScanner?.startScan(
            null,
            scanSettings,
            scanCallback
        )

        awaitClose {
            Log.d("BrushBuddy", "Stopping BLE scan")

            bluetoothLeScanner?.stopScan(scanCallback)
        }
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