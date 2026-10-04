package com.elnaz.brushbuddy.data.bluetooth
import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
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
import java.util.LinkedList
import java.util.Queue
import java.util.UUID


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
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt.services
                    services.forEach { service ->
                        Log.d("BrushBuddy","gatt service uuid : ${service.uuid}"
                        )
                        service.characteristics.forEach { characteristic ->
                            val canNotify = characteristic.properties and
                                    BluetoothGattCharacteristic.PROPERTY_NOTIFY !=0;
                            Log.d("BrushBuddy",
                                "gatt characteristic uuid : ${characteristic.uuid} " +
                                        "properties: 0x${characteristic.properties.toString(16)} " +
                                        "canNotify : $canNotify")

                        }
                    }
//                    finAllCharacteristic(gatt)
                    findStatusBatteryTimeCharacteristic(gatt)
//                    enableOralBFF04Notifications(gatt) /* just for uuid CHAR_FF04_UUID */
                }
            }
            val SERVICE_UUID: UUID = UUID.fromString("a0f0ff00-5047-4d53-8208-4f72616c2d42")
            // Standard BLE Client Characteristic Configuration Descriptor (CCCD) UUID
            val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            private val descriptorQueue: Queue<BluetoothGattCharacteristic> = LinkedList()
            val CHAR_FF04_UUID: UUID = UUID.fromString("a0f0ff04-5047-4d53-8208-4f72616c2d42")
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            fun enableOralBFF04Notifications(gatt: BluetoothGatt) {
                val service = gatt.getService(SERVICE_UUID) ?: return
                val characteristic = service.getCharacteristic(CHAR_FF04_UUID) ?: return
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                if (descriptor != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    }
                }
            }
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            private fun findStatusBatteryTimeCharacteristic(gatt: BluetoothGatt){
                descriptorQueue.clear()
                val service = gatt.getService((SERVICE_UUID))?: return
                for (i in listOf(4, 5, 8)){
                    val hexSuffix = String.format("%02x", i)
                    val charUuid = UUID.fromString("a0f0ff${hexSuffix}-5047-4d53-8208-4f72616c2d42")
                    val characteristic = service.getCharacteristic(charUuid)
                    if (characteristic != null) {
                        Log.d("BrushBuddy", "Queued characteristic: FF$hexSuffix")
                        descriptorQueue.add(characteristic)
                    } else {
                        Log.w("BrushBuddy", "Characteristic FF$hexSuffix not found on device")
                    }
                }
                processNextInQueue(gatt)
            }
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            private fun finAllCharacteristic(gatt: BluetoothGatt){
                descriptorQueue.clear()
                val service = gatt.getService((SERVICE_UUID))?: return
                // Loop through hex suffixes 04 to 0D (a0f0ff00-5047-4d53-8208-4f72616c2d42)
                for(i in 4..13){
                    val hexSuffix = String.format("%02x", i)
                    val charUuid = UUID.fromString("a0f0ff${hexSuffix}-5047-4d53-8208-4f72616c2d42")
                    val characteristic = service.getCharacteristic(charUuid)
                    if (characteristic != null) {
                        Log.d("BrushBuddy", "Queued characteristic: FF$hexSuffix")
                        descriptorQueue.add(characteristic)
                    } else {
                        Log.w("BrushBuddy", "Characteristic FF$hexSuffix not found on device")
                    }
                }
                processNextInQueue(gatt)
            }
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            private fun processNextInQueue(gatt: BluetoothGatt) {
                if (descriptorQueue.isEmpty()) {
                    Log.d("BrushBuddy", "All targeted Oral-B notifications successfully registered!")
                    return
                }
                // Take the next characteristic from the queue.
                val characteristic = descriptorQueue.poll() ?: return
                val shortName = characteristic.uuid.toString().substring(6, 8).uppercase() // e.g. "04"

                // A. Tell Android that we want notifications from this characteristic.
                val notificationEnabled = gatt.setCharacteristicNotification(characteristic, true)
                Log.d(
                    "BrushBuddy",
                    "Local notification FF$shortName enabled: $notificationEnabled"
                )
                // B. Find this characteristic's CCCD (0x2902).
                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                if (descriptor == null) {

                    Log.e("BrushBuddy","CCCD missing for FF$shortName")
                    // Can't enable this one, so try the next one.
                    processNextInQueue(gatt)
                    return
                }
                Log.d("BrushBuddy","Writing CCCD for FF$shortName")
                // Tell the toothbrush to send notifications.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {

                    @Suppress("DEPRECATION")
                    descriptor.value =
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
            }
            // Android calls this when the CCCD write above finishes.
            //
            // FF04 finished -> process FF05
            // FF05 finished -> process FF06
            // ...
            // FF0D finished -> queue becomes empty
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
                val characteristicUuid = descriptor.characteristic.uuid
                Log.d("BrushBuddy", "Descriptor write completed for " +
                            "$characteristicUuid status: $status")
                // Now enable the NEXT characteristic.
                processNextInQueue(gatt)
            }
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                val hexValue = value.joinToString(" ") {
                    "%02X".format(it.toInt() and 0xFF)
                }
                Log.d(
                    "BrushBuddy",
                    "NOTIFICATION uuid: ${characteristic.uuid} value: $hexValue"
                )
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
