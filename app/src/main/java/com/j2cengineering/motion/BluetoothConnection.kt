package com.j2cengineering.motion

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList


object BluetoothConnection {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    //private var bluetoothDevice: BluetoothDevice? = null
    //private var bluetoothGatt: BluetoothGatt? = null
    //private var bluetoothGattServices: MutableList<BluetoothGattService>? = null

    private var watchDogCount = 0
    private var queueWatchdog:Timer? = null
    private var queueWatchdogTask = object : TimerTask(){
        override fun run() {
            watchDogCount++

            if(watchDogCount > 1000)
            {

                forceEndOfOperation()
                watchDogCount = 0
            }
        }

    }

    private fun petWatchDog()
    {
        watchDogCount = 0
    }

    fun setUpBluetoothSystem(context:Context)
    {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if(bluetoothManager != null)
        {
            bluetoothAdapter = bluetoothManager!!.adapter

            if(scanning)
            {
                startScanning()
            }
        }

        if(queueWatchdog == null) {
            queueWatchdog = Timer()
            queueWatchdog?.scheduleAtFixedRate(queueWatchdogTask, 0, 1)
        }

    }

    fun checkBleHardware(context: Context):Boolean?
    {
        return context.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    val isBtEnabled:Boolean
    get(){
        bluetoothAdapter ?: return false

        return bluetoothAdapter!!.isEnabled
    }

    //Bluetooth scanning functions and such

    private var scanning:Boolean = false
    val isScanning:Boolean
    get(){
        return scanning
    }

    interface OnDeviceScanningListener{

        fun onDeviceFound(device: BluetoothDevice, rssi:Int, record: ByteArray)

        fun onScanStop()

        fun onScanStart()
    }

    private var deviceScanningListener : OnDeviceScanningListener? = null

    fun setDeviceScanningListener(newListener: OnDeviceScanningListener)
    {
        deviceScanningListener = newListener
    }

    fun removeDeviceScanningListener(oldListener: OnDeviceScanningListener){
        if(deviceScanningListener == oldListener)
        {
            deviceScanningListener = null
        }
    }

    private val deviceFoundCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if(deviceScanningListener != null) {
                deviceScanningListener?.onDeviceFound(
                    result!!.device,
                    result.rssi,
                    result.scanRecord!!.bytes
                )
            }
        }
    }

    fun startScanning()
    {
        val filterBuilder = ScanFilter.Builder()
        val builder = ScanSettings.Builder()
        val filters = ArrayList<ScanFilter>()
        filterBuilder.setServiceUuid(HLAServiceDefines.HLAServiceUUID)
        filters.add(filterBuilder.build())



        try {
            scanning = true

            if(bluetoothAdapter != null) {
                bluetoothAdapter!!.bluetoothLeScanner.startScan(
                    filters,
                    builder.build(),
                    deviceFoundCallback
                )
                deviceScanningListener?.onScanStart()
            }
        }catch(e:SecurityException)
        {
            return
        }
    }

    fun stopScanning(){
        val deviceFoundCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                //deviceScanningListener?.onDeviceFound(result!!.device, result.rssi, result.scanRecord!!.bytes)
                deviceScanningListener?.onScanStop()
            }
        }

        try {
            scanning = false
            if(bluetoothAdapter != null) {
                deviceScanningListener?.onScanStop()
                bluetoothAdapter!!.bluetoothLeScanner.stopScan(deviceFoundCallback)
            }
        }
        catch(e:SecurityException)
        {
            return
        }
    }

    fun getGattIsConnected(aGatt: BluetoothGatt?): Boolean
    {
        return if(aGatt != null) {
            try {
                val status =
                    bluetoothManager!!.getConnectionState(aGatt.device, BluetoothProfile.GATT)

                (status == BluetoothProfile.STATE_CONNECTED)
            } catch(e:SecurityException) {
                false
            }

        } else{
            false
        }
    }


    fun getDevice(deviceAddress:String): BluetoothDevice?{
        return bluetoothAdapter!!.getRemoteDevice(deviceAddress)
    }


    enum class TxQueueItemType
    {SubscribeCharacteristic, ReadCharacteristic, WriteCharacteristic, ConnectDevice, DisconnectDevice, MtuRequest}
    private data class TxQueueItem(val characteristic: BluetoothGattCharacteristic?, val gatt: BluetoothGatt?, val dataToWrite: ByteArray?, val enabled: Boolean?, var address: String?, var type: TxQueueItemType) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TxQueueItem

            if (characteristic != other.characteristic) return false
            if (gatt != other.gatt) return false
            if (dataToWrite != null) {
                if (other.dataToWrite == null) return false
                if (!dataToWrite.contentEquals(other.dataToWrite)) return false
            } else if (other.dataToWrite != null) return false
            if (enabled != other.enabled) return false
            if (address != other.address) return false
            if (type != other.type) return false

            return true
        }

        override fun hashCode(): Int {
            var result = characteristic?.hashCode() ?: 0
            result = 31 * result + (gatt?.hashCode() ?: 0)
            result = 31 * result + (dataToWrite?.contentHashCode() ?: 0)
            result = 31 * result + (enabled?.hashCode() ?: 0)
            result = 31 * result + (address?.hashCode() ?: 0)
            result = 31 * result + type.hashCode()
            return result
        }
    }

    private val operationQueue = ConcurrentLinkedQueue<TxQueueItem>()
    private var pendingOperation: TxQueueItem? = null

    @Synchronized
    fun doNextOperation()
    {
        if(pendingOperation != null)
        {
            Log.e("Bluetooth", "doNextOperation() called when on operation is pending! Aborting.")
            return
        }

        val operation = operationQueue.poll() ?: run{
            return
        }

        petWatchDog()
        pendingOperation = operation

        when(operation.type)
        {
            TxQueueItemType.WriteCharacteristic ->
            {
                writeDataToCharacteristic(operation.characteristic!!, operation.gatt!!, operation.dataToWrite)
                Log.d("Bluetooth Queue", "Writing to " + operation.characteristic.uuid.toString())
            }

            TxQueueItemType.SubscribeCharacteristic -> {
                setNotificationForCharacteristic(operation.characteristic!!, operation.gatt!!, operation.enabled!!)
                Log.d("Bluetooth", "Starting desc write: ${operation.characteristic.uuid}")
            }

            TxQueueItemType.ReadCharacteristic -> {
                requestCharacteristicValue(operation.characteristic!!, operation.gatt!!)
                Log.d("Bluetooth", "Starting read: ${operation.characteristic.uuid}")
            }

            TxQueueItemType.ConnectDevice ->{

                try {
                    operation.gatt!!.connect()
                }catch(e:SecurityException)
                {
                    Log.e("BluetoothConnection", "No Permission for connect:" + e.message)

                }

                Log.d("Bluetooth", "Starting connect: ${operation.characteristic?.uuid}")
            }

            TxQueueItemType.DisconnectDevice -> {
                disconnect(operation.gatt)
                Log.d("Bluetooth", "Starting disconnect: ${operation.characteristic?.uuid}")
            }

            TxQueueItemType.MtuRequest -> {
                try {
                    operation.gatt?.requestMtu(operation.dataToWrite?.get(0)?.toInt() ?: 64)
                }catch(e:SecurityException)
                {
                    Log.e("BluetoothConnection", "No Permission for mtu change:" + e.message)

                }
            }

//            TxQueueItemType.BackgroundDisconnect -> {
//                disconnect(operation.gatt)
//                Log.d("Bluetooth", "Starting background disconnect: ${operation.characteristic?.uuid}")
//            }
        }
    }

    /* disconnect the device. It is still possible to reconnect to it later with this Gatt client */
    private fun disconnect(aGatt: BluetoothGatt?)
    {
        try {
            aGatt?.disconnect()
        }
        catch(e:SecurityException)
        {
            Log.e("BluetoothConnection", "No Permission for disconnect:" + e.message)

        }
    }

    /* set new value for particular characteristic */
    private fun writeDataToCharacteristic(ch: BluetoothGattCharacteristic, gatt:BluetoothGatt, dataToWrite: ByteArray?)
    {
        if(bluetoothAdapter == null) return

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                if (dataToWrite != null) {
                    gatt.writeCharacteristic(
                        ch,
                        dataToWrite,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                }
            }catch(e:SecurityException)
            {
                Log.e("BluetoothConnection", "No Permission for write:" + e.message)

            }
        }
        else{
            ch.value = dataToWrite
            try {
                gatt.writeCharacteristic(ch)
            }catch(e:SecurityException)
            {
                Log.e("BluetoothConnection", "No Permission for write:" + e.message)

            }
        }

    }

    /* enables/disables notification for characteristic */
    private fun setNotificationForCharacteristic(ch: BluetoothGattCharacteristic, gatt:BluetoothGatt, enabled: Boolean)
    {
        if(bluetoothAdapter == null) return

        try {
            gatt.setCharacteristicNotification(ch, enabled)

            val descriptor =
                ch.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            if (descriptor != null) {
                val value =
                    if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                {
                    gatt.writeDescriptor(descriptor, value)
                }
                else {
                    descriptor.value = value
                    gatt.writeDescriptor(descriptor)
                }
            }
        }catch(e:SecurityException)
        {
            Log.e("BluetoothConnection", "No Permission for notification:" + e.message)

        }
    }

    private fun requestCharacteristicValue(ch: BluetoothGattCharacteristic?, gatt: BluetoothGatt?) {
        if (bluetoothAdapter == null || gatt == null) return
        try {
            val readSucceeded = gatt.readCharacteristic(ch)

            if(!readSucceeded)
            {
                Log.e("Bluetooth", "Read request has failed")
            }
        }
        catch(e:SecurityException)
        {
            Log.e("BluetoothConnection", "No Permission for read:" + e.message)

        }
    }

    /* queues enables/disables notification for characteristic */
    @Synchronized
    fun queueSetNotificationForCharacteristic(ch: BluetoothGattCharacteristic, gatt:BluetoothGatt, enabled: Boolean)
    {

        operationQueue.add(TxQueueItem(
            ch,
            gatt,
            null,
            enabled,
            null,
            TxQueueItemType.SubscribeCharacteristic
        ))

        if(pendingOperation == null)
        {
            doNextOperation()
        }
    }

    /* queues enables/disables notification for characteristic */
    @Synchronized
    fun queueWriteDataToCharacteristic(ch: BluetoothGattCharacteristic, gatt:BluetoothGatt, dataToWrite: ByteArray)
    {

        operationQueue.add(TxQueueItem(
            ch,
            gatt,
            dataToWrite,
            null,
            null,
            TxQueueItemType.WriteCharacteristic
        )
        )

        if(pendingOperation == null)
        {
            doNextOperation()
        }
    }

    /* request to fetch newest value stored on the remote device for particular characteristic */
    @Synchronized
    fun queueRequestCharacteristicValue(ch: BluetoothGattCharacteristic, gatt: BluetoothGatt)
    {
        operationQueue.add(TxQueueItem(
            ch,
            gatt,
            null,
            null,
            null,
            TxQueueItemType.ReadCharacteristic
        ))

        if(pendingOperation == null)
        {
            doNextOperation()
        }
    }

    @Synchronized
    fun queueRequestConnectDevice(address: String, gatt:BluetoothGatt)
    {
        operationQueue.add(TxQueueItem(
            null,
            gatt,
            null,
            null,
            address,
            TxQueueItemType.ConnectDevice
        ))

        if(pendingOperation == null)
        {
            doNextOperation()
        }
    }

    @Synchronized
    fun queueRequestDisconnectDevice(aGatt: BluetoothGatt)
    {

        operationQueue.add(TxQueueItem(
            null,
            aGatt,
            null,
            null,
            null,
            TxQueueItemType.DisconnectDevice
        ))

        if(pendingOperation == null)
        {
            doNextOperation()
        }
    }

//    @Synchronized
//    fun queueRequestBackgroundDisconnectDevice(aGatt:BluetoothGatt)
//    {
//        operationQueue.add(TxQueueItem(
//            null,
//            aGatt,
//            null,
//            null,
//            null,
//            TxQueueItemType.BackgroundDisconnect
//        ))
//
//        if(pendingOperation == null)
//        {
//            doNextOperation()
//        }
//    }

    @Synchronized
    fun queueRequestMtuUpdate(aGatt:BluetoothGatt, newMtu:Byte)
    {
        operationQueue.add(TxQueueItem(
            null,
            aGatt,
            byteArrayOf(newMtu),
            null,
            null,
            TxQueueItemType.MtuRequest
        ))

        if(pendingOperation == null)
        {
            doNextOperation()
        }
    }

    @Synchronized
    fun signalEndOfOperation(operationType:TxQueueItemType)
    {
        GlobalScope.launch {

            if (pendingOperation != null && pendingOperation?.type?.ordinal == operationType.ordinal) {
                pendingOperation = null
                if (operationQueue.isNotEmpty()) {
                    doNextOperation()
                }
            }
        }
    }

    @Synchronized
    private fun forceEndOfOperation()
    {
        GlobalScope.launch {
            if (pendingOperation != null) {
                Log.e("Bluetooth", "Operation Watch Dog Triggered")
                pendingOperation = null
                if (operationQueue.isNotEmpty()) {
                    doNextOperation()
                }
            }
        }
    }
}