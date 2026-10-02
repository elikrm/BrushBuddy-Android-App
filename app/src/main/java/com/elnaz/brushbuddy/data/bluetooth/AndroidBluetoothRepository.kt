package com.elnaz.brushbuddy.data.bluetooth
import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.elnaz.brushbuddy.models.BluetoothDeviceModel
import com.elnaz.brushbuddy.models.BluetoothState
import com.elnaz.brushbuddy.models.BrushingStatus
import com.elnaz.brushbuddy.models.ConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Arrays


//MutableStateFlow -> repository can change the value
//StateFlow -> other classes can observe it but shouldn't change it
class AndroidBluetoothRepository(private val context: Context
) : BluetoothRepository {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

//    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
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
        val bluetoothLeScanner =
            bluetoothAdapter.bluetoothLeScanner
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
        _scannedDevices.value = emptyList()

        val scanFilters = listOf(
            ScanFilter.Builder().build()
        )
        bluetoothLeScanner?.startScan(
            scanFilters,
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
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connectToDevice(device: BluetoothDeviceModel): Flow<ConnectionState> = callbackFlow{
        val bluetoothDevice =
            bluetoothAdapter.getRemoteDevice(device.address)
        trySend(ConnectionState.CONNECTING)
        val gattCallback = object : BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        trySend(ConnectionState.CONNECTED)
                        gatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED-> {
                        trySend(ConnectionState.DISCONNECTED)
                        close() // Close the flow channel
                    }
                }
            }
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt.services
                    services.forEach { service ->
                        Log.d(
                            "BrushBuddy",
                            "gatt service uuid : ${service.uuid}"
                        )
                        service.characteristics.forEach { characteristic ->
                            val canNotify = characteristic.properties and
                                    BluetoothGattCharacteristic.PROPERTY_NOTIFY !=0;
                            Log.d(
                                "BrushBuddy",
                                "gatt characteristic uuid : ${characteristic.uuid} " +
                                        "properties: 0x${characteristic.properties.toString(16)} " +
                                        "canNotify : $canNotify")

                        }
                    }
                }
            }
        }
        // Next Step: Call connectGatt
        val bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback)

        // Clean up the connection when the Flow collector cancels or finishes
        awaitClose {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        }
    }
    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }
    @RequiresPermission(
        allOf = [
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ]
    )
    fun testSimpleScan() {
    //learned from https://github.com/JimSeker/bluetooth/tree/master/BLEscannerDemo
        val scanner = bluetoothAdapter.bluetoothLeScanner
        Log.d(
            "BrushBuddy",
            "Simple scanner null = ${scanner == null}"
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = listOf(
            ScanFilter.Builder().build()
        )

        val callback = object : ScanCallback() {

            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {
                Log.d(
                    "BrushBuddy",
                    "SIMPLE SCAN FOUND: ${result.device.name}, RSSI=${result.rssi}"
                )
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BrushBuddy", "SIMPLE SCAN FAILED: $errorCode")
            }
        }

        Log.d("BrushBuddy", "Starting SIMPLE scan")

        scanner?.startScan(
            filters,
            settings,
            callback
        )
    }
}