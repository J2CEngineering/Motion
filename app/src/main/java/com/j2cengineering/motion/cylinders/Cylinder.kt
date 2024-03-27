package com.j2cengineering.motion.cylinders

import android.bluetooth.BluetoothGattCallback
import com.j2cengineering.motion.ChartDataSetManager
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

abstract class Cylinder {

    data class TimeValues(
        var day: Int = 0,
        var month: Int = 0,
        var year: Int = 0,
        var hour: Int = 0,
        var minute: Int = 0,
        var second: Int = 0
    )

    data class AlertValues(var date:String = "", var type:Int = 0, var info:String = "")

    var connected = false
        protected set

    var available = false
        protected set

    var serialnumber = ""
        protected set

    var name = "Some Name"

    var setPosition: Int = 0
        protected set

    var currPosition: Int = 0
        protected set

    var realPosition: Int = 0
        protected set

    var calHigh: Int = 0
        protected set

    var calLow: Int = 0
        protected set

    var QVLAStep: Int = 0
        protected set

    var temperature = 0.0
        protected set

    var loggedIn = false
        protected set

    var manualControl = false
        protected set

    var calibrationMode = false
        protected set

    var purging = false
        protected set

    var pressure1: Double = 0.0
        protected set

    var pressure2: Double = 0.0
        protected set

    var cylinderTime = TimeValues()
        protected set

    var alertCount: Int = 0
        protected set

    var address: String = ""
        protected set

    var isSaved: Boolean = false
        protected set

    var username: String = ""

    var password: String = ""

    var chartData = ChartDataSetManager()

    protected var basicDataRequested = false
    protected var fullDataRequested = false
    protected var timeDataRequested = false

    val currentAlert = AlertValues()

    @Serializable
    data class CylinderData(val type: String, val name: String, val address: String, val username: String, val password: String)

    abstract fun getCylinderData() : CylinderData

    open fun startBasicData() {
        basicDataRequested = true
    }

    open fun stopBasicData()
    {
        basicDataRequested = false
    }


    open fun startFullData()
    {
        fullDataRequested = true
    }

    open fun stopFullData()
    {
        fullDataRequested = false
    }

    open fun startTimeData()
    {
        timeDataRequested = true
    }

    open fun stopTimeData()
    {
        timeDataRequested = false
    }

    abstract fun connectCylinder()

    abstract fun disconnectCylinder()

    abstract fun clearLiveChart()

    interface OnBasicDataListener{
        fun onConnectionChange(cylinder: Cylinder)

        fun onSetPositionChange(cylinder: Cylinder)

        fun onCurrPositionChange(cylinder: Cylinder)

        fun onStatusChange(cylinder: Cylinder)

        fun onAlertChange(cylinder: Cylinder)
    }

    private val basicDataListeners: ArrayList<OnBasicDataListener>  = ArrayList<OnBasicDataListener>()

    fun addBasicDataListener(newListener: OnBasicDataListener)
    {
        for(listener in basicDataListeners)
        {
            if(newListener == listener)
            {
                return
            }
        }

        basicDataListeners.add(newListener)
        startBasicData()

    }

    fun removeBasicDataListener(oldListener: OnBasicDataListener)
    {
        val deadListeners = ArrayList<OnBasicDataListener>()

        for(listener in basicDataListeners)
        {
            if(listener == oldListener)
            {
                deadListeners.add(oldListener)
            }
        }

        for(aListener in deadListeners)
        {
            basicDataListeners.remove(aListener)
        }

        if(basicDataListeners.isEmpty())
        {
            stopBasicData()
        }
    }

    protected fun getBasicDataListeners():ArrayList<OnBasicDataListener>
    {
        return basicDataListeners
    }

    interface OnFullDataListener{
        fun onPressureOneChange(cylinder: Cylinder)

        fun onPressureTwoChange(cylinder: Cylinder)

        fun onAccelChange(cylinder: Cylinder)

        fun onTemperatureChange(cylinder: Cylinder)

        fun onChartChange(cylinder: Cylinder)
    }

    private var fullDataListeners: ArrayList<OnFullDataListener> = ArrayList<OnFullDataListener>()

    fun addFullDataListener(newListener: OnFullDataListener)
    {
        for(listener in fullDataListeners)
        {
            if(newListener == listener)
            {
                return
            }
        }

        fullDataListeners.add(newListener)
    }

    protected fun getFullDataListener():ArrayList<OnFullDataListener>
    {
        return fullDataListeners
    }

    fun removeFullDataListener(oldListener: OnFullDataListener)
    {
        val deadListeners = ArrayList<OnFullDataListener>()

        for(listener in fullDataListeners)
        {
            if(listener == oldListener)
            {
                deadListeners.add(oldListener)
            }
        }

        for(aListener in deadListeners)
        {
            fullDataListeners.remove(aListener)
        }

    }

    interface OnTimeChangeListener{
        fun onCylinderTimeChange(cylinder: Cylinder)
    }

    private var timeDataListeners: ArrayList<OnTimeChangeListener> = ArrayList<OnTimeChangeListener>()

    fun addTimeDataListener(newListener: OnTimeChangeListener)
    {
        for(listener in timeDataListeners)
        {
            if(newListener == listener)
            {
                return
            }
        }

        timeDataListeners.add(newListener)
    }

    protected fun getTimeDataListeners():ArrayList<OnTimeChangeListener>
    {
        return timeDataListeners
    }

    fun removeTimeDataListener(oldListener: OnTimeChangeListener)
    {
        val deadListeners = ArrayList<OnTimeChangeListener>()

        for(listener in timeDataListeners)
        {
            if(listener == oldListener)
            {
                deadListeners.add(oldListener)
            }
        }

        for(aListener in deadListeners)
        {
            timeDataListeners.remove(aListener)
        }

    }


    interface OnLogInListener
    {
        fun onLogInUpdate(cylinder: Cylinder, status: String)
    }

    private var logInListeners: ArrayList<OnLogInListener> = ArrayList()

    fun addLogInListener(newListener: OnLogInListener)
    {
        for(listener in logInListeners)
        {
            if(newListener == listener)
            {
                return
            }
        }

        logInListeners.add(newListener)

    }

    protected fun getLogInListeners(): ArrayList<OnLogInListener>
    {
        return logInListeners
    }

    fun removeLogInListener(oldListener: OnLogInListener)
    {
        val deadListeners = ArrayList<OnLogInListener>()

        for(listener in timeDataListeners)
        {
            if(listener == oldListener)
            {
                deadListeners.add(oldListener)
            }
        }

        for(aListener in deadListeners)
        {
            logInListeners.remove(aListener)
        }

    }

    interface OnAlertChangeListener
    {
        fun onAlertChange(cylinder: Cylinder, alertValues: AlertValues)
    }

    private var alertChangeListeners: ArrayList<OnAlertChangeListener> = ArrayList()

    fun addAlertChangeListener(newListener: OnAlertChangeListener)
    {
        for(listener in alertChangeListeners)
        {
            if(newListener == listener)
            {
                return
            }
        }

        alertChangeListeners.add(newListener)
    }

    protected fun getAlertChangeListeners(): ArrayList<OnAlertChangeListener>
    {
        return alertChangeListeners
    }

    fun removeAlertChangeListener(oldListener: OnAlertChangeListener)
    {
        val deadListeners = ArrayList<OnAlertChangeListener>()

        for(listener in alertChangeListeners)
        {
            if(listener == oldListener)
            {
                deadListeners.add(oldListener)
            }
        }

        for(aListener in deadListeners)
        {
            alertChangeListeners.remove(aListener)
        }

    }


    abstract fun attemptLogIn(name: String, password:String)

    abstract fun cancelLogIn()

    abstract fun sendLogout()

    abstract fun attemptPasswordChange(oldPassword: String, newPassword: String)

    abstract fun cancelPasswordChange()

    abstract fun setManualControl()

    abstract fun setAutomaticControl()

    abstract fun setCylinderPercentage(percentage:Int)

    abstract fun toggleCalibrationMode()

    abstract fun setCalibrationHigh()

    abstract fun setCalibrationLow()

    abstract fun writeDeviceTime(newTime: LocalDateTime)

    abstract fun triggerPurge()

    abstract fun requestAlertInfo(alertNum:Int)

}
