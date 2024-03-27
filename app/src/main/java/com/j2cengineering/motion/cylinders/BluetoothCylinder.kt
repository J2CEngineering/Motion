package com.j2cengineering.motion.cylinders

import android.bluetooth.*
import android.content.Context
import com.j2cengineering.motion.ApplicationConfiguration
import com.j2cengineering.motion.BluetoothConnection
import com.j2cengineering.motion.HLAServiceDefines
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.logging.Handler
import kotlin.experimental.and
import kotlin.experimental.or

class BluetoothCylinder(private val deviceAddress: String, name: String = "New Cylinder", saved: Boolean = false): Cylinder() {

    private val ACCELSENSITIVITY: Double = 6.1035156E-5

    private var bluetoothDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothGattServices: MutableList<BluetoothGattService>? = null

    private var currStatus:Byte = 0x00

    private var disconnectRequested = false
    //private var disconnected: Boolean = false

    var manufactureName: String = ""
        private set

    var modelNumber: String = ""
        private set

    var hardwareRevision: String = ""
        private set

    var firmwareRevision: String = ""
        private set

    var softwareRevision: String = ""
        private set


    private var hlaCylinderPercentageCharacteristic: BluetoothGattCharacteristic? = null
    private var hlaCylinderStatusCharacteristic: BluetoothGattCharacteristic? = null
    private var hlaCylinderPositionCharacteristic: BluetoothGattCharacteristic? = null
    private var hlaCylinderAccelCharacteristic: BluetoothGattCharacteristic? = null
    private var hlaCommandCharacteristic: BluetoothGattCharacteristic? = null
    private var hlaDeviceNameCharacteristic: BluetoothGattCharacteristic? = null
    private var hlaCylinderTimeCharacteristic: BluetoothGattCharacteristic? = null
    var hlaDataTransferCharacteristic: BluetoothGattCharacteristic? = null
    var alertCountCharacteristic: BluetoothGattCharacteristic? = null
    var currentAlertCharacteristic: BluetoothGattCharacteristic? = null
    var alertDateCharacteristic: BluetoothGattCharacteristic? = null
    var alertTypeCharacteristic: BluetoothGattCharacteristic? = null
    var alertInfoCharacteristic: BluetoothGattCharacteristic? = null

    init {
        address = deviceAddress
        super.name = name
        super.isSaved = saved
    }

    override fun getCylinderData(): CylinderData {

        return CylinderData("Bluetooth", name, address, username, password)
    }

    override fun connectCylinder() {
        if (this.bluetoothGatt != null && this.bluetoothGatt!!.device.address == this.deviceAddress) {
            try {
                BluetoothConnection.queueRequestConnectDevice(deviceAddress, bluetoothGatt!!)
                disconnectRequested = false
            } catch (_: SecurityException) {

            }
        }
    }

    override fun disconnectCylinder() {
        if (bluetoothGatt != null) {
            if (BluetoothConnection.getGattIsConnected(bluetoothGatt)) {
                disconnectRequested = true
                BluetoothConnection.queueRequestDisconnectDevice(bluetoothGatt!!)
            }
        }
    }

    fun connectCylinder(connection: BluetoothConnection, context: Context?) {
        disconnectRequested = false
        if (this.bluetoothGatt != null && this.bluetoothGatt!!.device.address == this.deviceAddress) {
            try {
                BluetoothConnection.queueRequestConnectDevice(deviceAddress, bluetoothGatt!!)
            } catch (_: SecurityException) {

            }
        } else {
            if (bluetoothGatt != null) {
                if (BluetoothConnection.getGattIsConnected(bluetoothGatt)) {
                    //disconnect the old GATT
                    BluetoothConnection.queueRequestDisconnectDevice(bluetoothGatt!!)
                }
            }

            bluetoothDevice = BluetoothConnection.getDevice(deviceAddress)

            if (bluetoothDevice != null) {
                try {

                    bluetoothGatt = bluetoothDevice!!.connectGatt(
                        context,
                        true,
                        callbacks)
                } catch (_: SecurityException) {

                }
            }
        }
    }

    private val callbacks = CylinderBluetoothCallbacks(this)

    inner class CylinderBluetoothCallbacks(private val cylinder: BluetoothCylinder) : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            gatt: BluetoothGatt?,
            status: Int,
            newState: Int
        ) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connected = true

//                if (getBasicDataListener() != null) {
//                    getBasicDataListener()?.onConnectionChange(cylinder)
//                }

                for(listener in getBasicDataListeners())
                {
                    listener.onConnectionChange(cylinder)
                }

                BluetoothConnection.signalEndOfOperation(BluetoothConnection.TxQueueItemType.ConnectDevice)

                BluetoothConnection.queueRequestMtuUpdate(bluetoothGatt!!, 127)


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connected = false
//                if (getBasicDataListener() != null) {
//                    getBasicDataListener()?.onConnectionChange(cylinder)
//                }
                for(listener in getBasicDataListeners())
                {
                    listener.onConnectionChange(cylinder)
                }

                try {

                    //if(!disconnectRequested) {
                        //BluetoothConnection.queueRequestConnectDevice(deviceAddress, bluetoothGatt!!)
                    //}
                    //else {
                        //bluetoothGatt?.close()
                        //bluetoothGatt = null
                    //}
                } catch (_: SecurityException) {

                } catch (_: Exception) {
                }
                BluetoothConnection.signalEndOfOperation(BluetoothConnection.TxQueueItemType.DisconnectDevice)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            try {
                if (bluetoothGatt != null) bluetoothGatt!!.discoverServices()
            } catch (_: SecurityException) {

            }

            BluetoothConnection.signalEndOfOperation(BluetoothConnection.TxQueueItemType.MtuRequest)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (bluetoothGattServices != null && bluetoothGattServices?.size!! > 0) {
                    bluetoothGattServices!!.clear()
                }
                // keep reference to all services in local array:
                if (bluetoothGatt != null) {
                    bluetoothGattServices = bluetoothGatt!!.services
                }

                for (service in bluetoothGattServices!!) {
                    if (service.uuid == HLAServiceDefines.HLAServiceUUID.uuid || service.uuid == HLAServiceDefines.HLAAlertServiceUUID.uuid || service.uuid == HLAServiceDefines.DeviceInfoServiceUUID.uuid) {
                        getCharacteristicsForService(service)
                    }
                }
            }
        }

//                            override fun onCharacteristicRead(
//                                gatt: BluetoothGatt?,
//                                characteristic: BluetoothGattCharacteristic?,
//                                status: Int
//                            ) {
//                                // we got response regarding our request to fetch characteristic value
//                                if (status == BluetoothGatt.GATT_SUCCESS) {
//                                    // and it success, so we can get the value
//                                    getCharacteristicValue(characteristic)
//                                }
//                                //txSemaphore.release()
//
////                                if (pendingOperation?.type?.ordinal == TxQueueItemType.ReadCharacteristic.ordinal)
////                                {
////                                    Log.d("Bluetooth", "Received read: ${characteristic?.uuid.toString()}")
////                                    signalEndOfOperation()
////
////                                }
//
//                            }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            GlobalScope.launch {
                processCharacteristicValue(characteristic, value)
            }

            BluetoothConnection.signalEndOfOperation(BluetoothConnection.TxQueueItemType.ReadCharacteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            GlobalScope.launch {
                processCharacteristicValue(characteristic, value)
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {


            BluetoothConnection.signalEndOfOperation(BluetoothConnection.TxQueueItemType.SubscribeCharacteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {

//            if(logInSemaphore != null && characteristic?.uuid == hlaDataTransferCharacteristic?.uuid)
//            {
//                logInSemaphore!!.release()
//            }

            GlobalScope.launch {
                if (doingLogIn && characteristic?.uuid == hlaDataTransferCharacteristic?.uuid) {
                    val data = byteArrayOf(0x64.toByte())

                    BluetoothConnection.queueWriteDataToCharacteristic(
                        hlaCommandCharacteristic!!,
                        bluetoothGatt!!,
                        data
                    )
                }

                if (doingPasswordChange && characteristic?.uuid == hlaDataTransferCharacteristic?.uuid) {
                    val data = byteArrayOf(0x66.toByte())

                    BluetoothConnection.queueWriteDataToCharacteristic(
                        hlaCommandCharacteristic!!,
                        bluetoothGatt!!,
                        data
                    )
                }

                if (gettingAlert && characteristic?.uuid == currentAlertCharacteristic!!.uuid) {
                    alertTypeCharacteristic?.let {
                        if (gatt != null) {
                            BluetoothConnection.queueRequestCharacteristicValue(
                                it, gatt
                            )
                        }
                    }

                    alertInfoCharacteristic?.let {
                        if (gatt != null) {
                            BluetoothConnection.queueRequestCharacteristicValue(
                                it, gatt
                            )
                        }
                    }

                    alertDateCharacteristic?.let {
                        if (gatt != null) {
                            BluetoothConnection.queueRequestCharacteristicValue(
                                it, gatt
                            )
                        }
                    }
                }
            }
            BluetoothConnection.signalEndOfOperation(BluetoothConnection.TxQueueItemType.WriteCharacteristic)
        }

    }



    private fun processCharacteristicValue(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        if(value.isEmpty())
        {
            return
        }

        when (characteristic.uuid) {

            HLAServiceDefines.HLACylinderPercentageUUID.uuid -> {
                val oldPosition = setPosition
                setPosition = value[0].toInt()
                //chartData.addLivePercentage(value[0])

                if(oldPosition != setPosition) {
//                    if (getBasicDataListener() != null) {
//                        getBasicDataListener()?.onSetPositionChange(this)
//                    }
                    for(listener in getBasicDataListeners())
                    {
                        listener.onSetPositionChange(this)
                    }
                }
            }

            HLAServiceDefines.HLACylinderPositionUUID.uuid -> {
                if (value.size < 3) {
                    return
                }

                val oldPosition = currPosition
                currPosition = value[0].toInt()


                //chartData.addLivePosition(value[0])

                val oldHigh = calHigh
                calHigh = value[1].toInt()

                val oldLow = calLow
                calLow = value[2].toInt()

                if (value.size > 3) {
                    realPosition = value[3].toInt()

                    var builder = value[5].toInt() shl 8
                    builder = builder or value[4].toInt()

                    QVLAStep = builder

                }

                if(oldPosition != currPosition || oldHigh != calHigh || oldLow != calLow) {
//                    if (getBasicDataListener() != null) {
//                        getBasicDataListener()?.onCurrPositionChange(this)
//                    }
                    for(listener in getBasicDataListeners())
                    {
                        listener.onCurrPositionChange(this)
                    }
                }
            }

            HLAServiceDefines.HLAStatusUUID.uuid -> {


                if(value[0] != currStatus) {
                    loggedIn = (value[0] and 0x01) != 0.toByte()
                    manualControl = (value[0] and 0x04) != 0.toByte()
                    calibrationMode = (value[0] and 0x08) != 0.toByte()
                    purging = (value[0] and 0x10) != 0.toByte()
                    currStatus = value[0]

//                    if (getBasicDataListener() != null) {
//                        getBasicDataListener()?.onStatusChange(this)
//                    }
                    for(listener in getBasicDataListeners())
                    {
                        listener.onConnectionChange(this)
                    }
                }
            }

            //                                    HLAServiceDefines.HLADeviceNameUUID.uuid ->
            //                                    {
            //                                        dataController.bleDidChangeName(this, rawValue)
            //                                    }

            HLAServiceDefines.HLAAccelerometerDataUUID.uuid -> {
                var builder = value[1].toInt() shl 8
                builder = builder or value[0].toUByte().toInt()

                val accelX = builder.toDouble() * ACCELSENSITIVITY


                builder = value[3].toInt() shl 8
                builder = builder or value[2].toUByte().toInt()

                val accelY = builder.toDouble() * ACCELSENSITIVITY


                builder = value[5].toInt() shl 8
                builder = builder or value[4].toUByte().toInt()

                val accelZ = builder.toDouble() * ACCELSENSITIVITY


//                if(getFullDataListener() != null)
//                {
//                    getFullDataListener()?.onAccelChange(this)
//                }

                for(listener in getFullDataListener())
                {
                    listener.onAccelChange(this)
                }


                if (value.size > 6) {

                    builder = value[7].toInt() shl 8
                    builder = builder or value[6].toUByte().toInt()

                    val oldTemp = temperature
                    temperature = builder.toDouble()

                    if(ApplicationConfiguration.TemperatureUnitIsFahrenheit)
                    {
                        temperature = (temperature * (9.0 / 5.0)) + 32.0
                    }

                    if(temperature != oldTemp)
                    {



//                        if(getFullDataListener() != null)
//                        {
//                            getFullDataListener()?.onTemperatureChange(this)
//                        }

                        for(listener in getFullDataListener())
                        {
                            listener.onTemperatureChange(this)
                        }
                    }



                    builder = value[11].toInt() shl 8
                    builder = builder or value[10].toUByte().toInt()

                    val oldPressure1 = pressure1
                    pressure1 = builder.toDouble() / 100.0

                    if(pressure1 != oldPressure1)
                    {
//                        if(getFullDataListener() != null)
//                        {
//                            getFullDataListener()?.onPressureOneChange(this)
//                        }

                        for(listener in getFullDataListener())
                        {
                            listener.onPressureOneChange(this)
                        }
                    }

                    builder = value[13].toInt() shl 8
                    builder = builder or value[12].toUByte().toInt()

                    val oldPressure2 = pressure2
                    pressure2 = builder.toDouble() / 100.0

                    if(pressure2 != oldPressure2)
                    {
//                        if(getFullDataListener() != null)
//                        {
//                            getFullDataListener()?.onPressureTwoChange(this)
//                        }

                        for(listener in getFullDataListener())
                        {
                            listener.onPressureTwoChange(this)
                        }
                    }

                    //force = round((pressure2 * 12.12) - (pressure1 * 12.56))

                    chartData.addLivePercentage(setPosition.toByte())
                    chartData.addLivePosition(currPosition.toByte())

                    chartData.addLiveAccel(
                        accelX.toFloat(),
                        accelY.toFloat(),
                        accelZ.toFloat(),
                        0.0f,
                        temperature.toFloat()
                    )

                } else {
                    chartData.addLivePercentage(setPosition.toByte())
                    chartData.addLivePosition(currPosition.toByte())

                    chartData.addLiveAccel(
                        accelX.toFloat(),
                        accelY.toFloat(),
                        accelZ.toFloat(),
                        0.0f,
                        0.0f
                    )
                }
            }

            HLAServiceDefines.HLACylinderTimeUUID.uuid -> {
                cylinderTime.second = ((value[0].toInt() and 0x70) shr 4) * 10
                cylinderTime.second += (value[0].toInt() and 0x0F)

                cylinderTime.minute = ((value[1].toInt() and 0x70) shr 4) * 10
                cylinderTime.minute += (value[1].toInt() and 0x0F)

                cylinderTime.hour = ((value[2].toInt() and 0x30) shr 4) * 10
                cylinderTime.hour += (value[2].toInt() and 0x0F)

                cylinderTime.day = ((value[4].toInt() and 0x30) shr 4) * 10
                cylinderTime.day += (value[4].toInt() and 0x0F)

                cylinderTime.month = ((value[5].toInt() and 0x10) shr 4) * 10
                cylinderTime.month += (value[5].toInt() and 0x0F)

                val year = value[6].toUByte().toInt()

                cylinderTime.year = ((year and 0xF0) shr 4) * 10
                cylinderTime.year += (year and 0x0F)

//                if(getTimeDataListener() != null)
//                {
//                    getTimeDataListener()?.onCylinderTimeChange(this)
//                }

                for(listener in getTimeDataListeners())
                {
                    listener.onCylinderTimeChange(this)
                }
            }

            HLAServiceDefines.HLADataTransferUUID.uuid -> {
                //dataController.bleDidUpdateDataField(this, rawValue)


//                if(logInSemaphore != null) {
//                    logInSemaphore?.release()

                if(doingLogIn)
                {
                    doingLogIn = false
//                    if(getLogInListener() != null)
//                    {
//                        val statusString = String(value).trim{it <= ' '}
//
//                        getLogInListener()?.OnLogInUpdate(this, statusString)
//                    }

                    for(listener in getLogInListeners())
                    {

                        val statusString = String(value).trim{it <= ' '}
                        listener.onLogInUpdate(this, statusString)
                    }

                    BluetoothConnection.queueSetNotificationForCharacteristic(hlaDataTransferCharacteristic!!, bluetoothGatt!!, false)

                }

                if(doingPasswordChange)
                {
                    doingPasswordChange = false
//                    if(getLogInListener() != null)
//                    {
//                        val statusString = String(value).trim{it <= ' '}
//
//                        getLogInListener()?.OnLogInUpdate(this, statusString)
//                    }

                    for(listener in getLogInListeners())
                    {

                        val statusString = String(value).trim{it <= ' '}
                        listener.onLogInUpdate(this, statusString)
                    }

                    BluetoothConnection.queueSetNotificationForCharacteristic(hlaDataTransferCharacteristic!!, bluetoothGatt!!, false)

                }

            }


            HLAServiceDefines.AlertCountUUID.uuid -> {
                //dataController.alertServiceDidUpdateCount(this, rawValue)
                alertCount = value[0].toInt()

//                if (getBasicDataListener() != null) {
//                    getBasicDataListener()?.onAlertChange(this)
//                }
                for(listener in getBasicDataListeners())
                {
                    listener.onAlertChange(this)
                }
            }

            HLAServiceDefines.AlertTypeUUID.uuid -> {

                synchronized(this)
                {
                    currentAlert.type = value[0].toInt()

                    alertReadCount++

                    if(alertReadCount >= 3)
                    {
//                        if(getAlertChangeListener() != null)
//                        {
//                            gettingAlert = false
//                            getAlertChangeListener()?.OnAlertChange(this, currentAlert)
//                        }

                        for(listener in getAlertChangeListeners())
                        {
                            gettingAlert = false
                            listener.onAlertChange(this, currentAlert)
                        }
                    }
                }
            }

            HLAServiceDefines.AlertDateUUID.uuid ->{

                synchronized(this)
                {
                    currentAlert.date = String(value).trim{it <= ' '}

                    alertReadCount++

                    if(alertReadCount >= 3)
                    {
//                        if(getAlertChangeListener() != null)
//                        {
//                            gettingAlert = false
//                            getAlertChangeListener()?.OnAlertChange(this, currentAlert)
//                        }
                        for(listener in getAlertChangeListeners())
                        {
                            gettingAlert = false
                            listener.onAlertChange(this, currentAlert)
                        }

                    }
                }
            }

            HLAServiceDefines.AlertInfoUUID.uuid -> {

                synchronized(this)
                {
                    currentAlert.info = String(value).trim{it <= ' '}

                    alertReadCount++

                    if(alertReadCount >= 3)
                    {
//                        if(getAlertChangeListener() != null)
//                        {
//                            gettingAlert = false
//                            getAlertChangeListener()?.OnAlertChange(this, currentAlert)
//                        }
                        for(listener in getAlertChangeListeners())
                        {
                            gettingAlert = false
                            listener.onAlertChange(this, currentAlert)
                        }
                    }
                }
            }

            HLAServiceDefines.ManufactureNameUUID.uuid -> {
                manufactureName = getCharacteristicString(value)
            }

            HLAServiceDefines.ModelNumberUUID.uuid -> {
                modelNumber = getCharacteristicString(value)
            }

            HLAServiceDefines.SerialNumberUUID.uuid -> {
                serialnumber = String.format(
                    "%02X:%02X:%02X:%02X:%02X:%02X",
                    value[0],
                    value[1],
                    value[2],
                    value[3],
                    value[4],
                    value[5]
                )
            }

            HLAServiceDefines.HardwareRevisionUUID.uuid -> {
                hardwareRevision = getCharacteristicString(value)
            }

            HLAServiceDefines.FirmwareRevisionUUID.uuid -> {
                firmwareRevision = getCharacteristicString(value)
            }

            HLAServiceDefines.SoftwareRevisionUUID.uuid -> {
                softwareRevision = getCharacteristicString(value)
            }

        }
    }


    override fun startBasicData() {
        super.startBasicData()

        if(!connected)
        {
            return
        }
        if(hlaCylinderPercentageCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderPercentageCharacteristic!!,
                bluetoothGatt!!,
                true
            )
        }

        if(hlaCylinderPositionCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderPositionCharacteristic!!,
                bluetoothGatt!!,
                true
            )
        }

        if(hlaCylinderStatusCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderStatusCharacteristic!!,
                bluetoothGatt!!,
                true
            )
            BluetoothConnection.queueRequestCharacteristicValue(hlaCylinderStatusCharacteristic!!, bluetoothGatt!!)
        }

        if(alertCountCharacteristic != null && bluetoothGatt != null){
            BluetoothConnection.queueSetNotificationForCharacteristic(
                alertCountCharacteristic!!,
                bluetoothGatt!!,
                true
            )
            BluetoothConnection.queueRequestCharacteristicValue(alertCountCharacteristic!!, bluetoothGatt!!)
        }

    }

    override fun stopBasicData() {
        super.stopBasicData()

        if(!connected)
        {
            return
        }
        if(hlaCylinderPercentageCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderPercentageCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }

        if(hlaCylinderPositionCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderPositionCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }

        if(hlaCylinderStatusCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderStatusCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }

        if(alertCountCharacteristic != null && bluetoothGatt != null){
            BluetoothConnection.queueSetNotificationForCharacteristic(
                alertCountCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }
    }

    override fun startFullData() {
        super.startFullData()

        if(!connected)
        {
            return
        }
        if(hlaCylinderAccelCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderAccelCharacteristic!!,
                bluetoothGatt!!,
                true
            )
        }

//        if(hlaCylinderTimeCharacteristic != null && bluetoothGatt != null) {
//            BluetoothConnection.queueSetNotificationForCharacteristic(
//                hlaCylinderTimeCharacteristic!!,
//                bluetoothGatt!!,
//                true
//            )
//        }
        //BluetoothConnection.queueSetNotificationForCharacteristic(alertCountCharacteristic!!, bluetoothGatt!!, true)
        //BluetoothConnection.queueRequestCharacteristicValue(alertCountCharacteristic!!, bluetoothGatt!!)
    }

    override fun stopFullData() {
        super.stopFullData()

        if(!connected)
        {
            return
        }

        if(hlaCylinderAccelCharacteristic != null && bluetoothGatt != null) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderAccelCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }

//        if(hlaCylinderTimeCharacteristic != null && bluetoothGatt != null) {
//            BluetoothConnection.queueSetNotificationForCharacteristic(
//                hlaCylinderTimeCharacteristic!!,
//                bluetoothGatt!!,
//                false
//            )
//        }
        //BluetoothConnection.queueSetNotificationForCharacteristic(alertCountCharacteristic!!, bluetoothGatt!!, false)
    }

    override fun startTimeData() {
        super.startTimeData()

        if(!connected)
        {
            return
        }

        if(hlaCylinderTimeCharacteristic != null && bluetoothGatt != null)
        {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderTimeCharacteristic!!,
                bluetoothGatt!!,
                true
            )
        }
    }

    override fun stopTimeData() {
        super.stopTimeData()

        if(!connected)
        {
            return
        }

        if(hlaCylinderTimeCharacteristic != null && bluetoothGatt != null)
        {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaCylinderTimeCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }
    }

    /* get all characteristic for particular service and pass them to the UI callback */
    private fun getCharacteristicsForService(service: BluetoothGattService?) {
        if (service == null) return

        val chars = service.characteristics

        for (characteristic in chars) {
            when (characteristic.uuid) {
                HLAServiceDefines.HLACylinderPercentageUUID.uuid -> {
                    hlaCylinderPercentageCharacteristic = characteristic
                    if(basicDataRequested && connected) {
                        BluetoothConnection.queueSetNotificationForCharacteristic(characteristic, bluetoothGatt!!, true)
                    }
                }

                HLAServiceDefines.HLACylinderPositionUUID.uuid -> {
                    hlaCylinderPositionCharacteristic = characteristic
                    if(basicDataRequested && connected) {
                        BluetoothConnection.queueSetNotificationForCharacteristic(characteristic, bluetoothGatt!!, true)
                    }
                }

                HLAServiceDefines.HLAStatusUUID.uuid -> {
                    hlaCylinderStatusCharacteristic = characteristic
                    if(connected) {
                        if (basicDataRequested) {
                            BluetoothConnection.queueSetNotificationForCharacteristic(
                                characteristic,
                                bluetoothGatt!!,
                                true
                            )
                        }
                        BluetoothConnection.queueRequestCharacteristicValue(
                            characteristic,
                            bluetoothGatt!!
                        )
                    }
                }

                HLAServiceDefines.HLACommandUUID.uuid -> {
                    hlaCommandCharacteristic = characteristic
                }

                HLAServiceDefines.HLADeviceNameUUID.uuid -> {
                    hlaDeviceNameCharacteristic = characteristic
                }

                HLAServiceDefines.HLAAccelerometerDataUUID.uuid -> {
                    hlaCylinderAccelCharacteristic = characteristic

                    if(fullDataRequested && connected) {
                        BluetoothConnection.queueSetNotificationForCharacteristic(characteristic, bluetoothGatt!!, true)
                    }
                }

                HLAServiceDefines.HLACylinderTimeUUID.uuid -> {
                    hlaCylinderTimeCharacteristic = characteristic

                    if(timeDataRequested && connected) {
                        BluetoothConnection.queueSetNotificationForCharacteristic(characteristic, bluetoothGatt!!, true)
                    }
                }

                HLAServiceDefines.HLADataTransferUUID.uuid -> {
                    hlaDataTransferCharacteristic = characteristic
                    //queueSetNotificationForCharacteristic(characteristic, true)
                }

                HLAServiceDefines.AlertCountUUID.uuid -> {
                    alertCountCharacteristic = characteristic
                    if(basicDataRequested && connected) {
                        BluetoothConnection.queueSetNotificationForCharacteristic(characteristic, bluetoothGatt!!, true)
                    }
                }

                HLAServiceDefines.CurrentAlertUUID.uuid -> {
                    currentAlertCharacteristic = characteristic
                }

                HLAServiceDefines.AlertDateUUID.uuid -> {
                    alertDateCharacteristic = characteristic
                    //queueSetNotificationForCharacteristic(characteristic, true)
                }

                HLAServiceDefines.AlertTypeUUID.uuid -> {
                    alertTypeCharacteristic = characteristic
                    //queueSetNotificationForCharacteristic(characteristic, true)
                }

                HLAServiceDefines.AlertInfoUUID.uuid -> {
                    alertInfoCharacteristic = characteristic
                    //queueSetNotificationForCharacteristic(characteristic, true)
                }
            }

            if (characteristic.uuid != HLAServiceDefines.HLACommandUUID.uuid) {
                if(connected) {
                    BluetoothConnection.queueRequestCharacteristicValue(
                        characteristic,
                        bluetoothGatt!!
                    )
                }
            }
        }
    }

    private fun getCharacteristicString(rawValue: ByteArray): String {
        var i = 0
        val builder = StringBuilder()

        while (i < rawValue.size && rawValue[i] != 0.toByte()) {
            builder.append(String.format("%c", rawValue[i]))
            i++
        }
        return builder.toString()
    }

    private var doingLogIn = false

    override fun attemptLogIn(name: String, password: String) {
        doingLogIn = true
        val data = ByteArray(40)
        var i = 0
        for(c in name.toByteArray(Charset.defaultCharset()))
        {
            data[i] = c
            i++
        }

        i = 20

        for(c in password.toByteArray(Charset.defaultCharset()))
        {
            data[i] = c
            i++
        }

        if(connected) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaDataTransferCharacteristic!!,
                bluetoothGatt!!,
                true
            )
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaDataTransferCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun cancelLogIn() {
        doingLogIn = false

        if(connected) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaDataTransferCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }
    }

    override fun sendLogout() {
        val data = byteArrayOf(0x65.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCommandCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    private var doingPasswordChange = false

    override fun attemptPasswordChange(oldPassword: String, newPassword: String) {
        doingPasswordChange = true

        val data = ByteArray(40)
        var i = 0
        for(c in newPassword.toByteArray(Charset.defaultCharset()))
        {
            data[i] = c
            i++
        }

        i = 20

        for(c in oldPassword.toByteArray(Charset.defaultCharset()))
        {
            data[i] = c
            i++
        }

        if(connected) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaDataTransferCharacteristic!!,
                bluetoothGatt!!,
                true
            )
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaDataTransferCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun cancelPasswordChange() {
        doingPasswordChange = false

        if(connected) {
            BluetoothConnection.queueSetNotificationForCharacteristic(
                hlaDataTransferCharacteristic!!,
                bluetoothGatt!!,
                false
            )
        }
    }

    override fun setManualControl() {
        val data = byteArrayOf(0x01.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCommandCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun setAutomaticControl() {
        val data = byteArrayOf(0x02.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCommandCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun setCylinderPercentage(percentage: Int) {
        val data = byteArrayOf(percentage.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCylinderPercentageCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun toggleCalibrationMode() {
        val data = byteArrayOf(0x03.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCommandCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun setCalibrationHigh() {
        val data = byteArrayOf(0x04.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCommandCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun setCalibrationLow() {
        val data = byteArrayOf(0x05.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCommandCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun writeDeviceTime(newTime: LocalDateTime) {
        val newTimeValues = ByteArray(7)

        newTimeValues[0] = (((newTime.second / 10) shl 4).toByte())
        newTimeValues[0] = newTimeValues[0] or ((newTime.second % 10) and 0x0F).toByte()

        newTimeValues[1] = (((newTime.minute / 10) shl 4).toByte())
        newTimeValues[1] = newTimeValues[1] or ((newTime.minute % 10) and 0x0F).toByte()

        newTimeValues[2] = (((newTime.hour / 10) shl 4).toByte())
        newTimeValues[2] = newTimeValues[2] or ((newTime.hour % 10) and 0x0F).toByte()

        newTimeValues[4] = (((newTime.dayOfMonth / 10) shl 4).toByte())
        newTimeValues[4] = newTimeValues[4] or ((newTime.dayOfMonth % 10) and 0x0F).toByte()

        newTimeValues[5] = (((newTime.monthValue / 10) shl 4).toByte())
        newTimeValues[5] = newTimeValues[5] or ((newTime.monthValue % 10) and 0x0F).toByte()

        val shortenedYear = newTime.year % 100

        newTimeValues[6] = (((shortenedYear / 10) shl 4).toByte())
        newTimeValues[6] = newTimeValues[6] or ((shortenedYear % 10) and 0x0F).toByte()

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCylinderTimeCharacteristic!!,
                bluetoothGatt!!,
                newTimeValues
            )
        }
    }

    override fun triggerPurge() {
        val data = byteArrayOf(0x09.toByte())

        if(connected) {
            BluetoothConnection.queueWriteDataToCharacteristic(
                hlaCommandCharacteristic!!,
                bluetoothGatt!!,
                data
            )
        }
    }

    override fun clearLiveChart() {
        chartData.clearLiveData()
    }

    private var gettingAlert = false
    private var alertReadCount = 0

    override fun requestAlertInfo(alertNum:Int) {

        if(!gettingAlert) {
            alertReadCount = 0

            if(alertNum >= 0) {
                gettingAlert = true
            }
            val data = byteArrayOf(alertNum.toByte())

            if(connected) {
                BluetoothConnection.queueWriteDataToCharacteristic(
                    currentAlertCharacteristic!!,
                    bluetoothGatt!!,
                    data
                )
            }
        }
    }
}