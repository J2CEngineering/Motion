package com.j2cengineering.motion

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write


class ThreadSafeLineDataSet(aList: List<Entry>, label: String) : LineDataSet(aList, label)
{

    private val readWriteLock = ReentrantReadWriteLock(true)

    override fun addEntry(e: Entry?): Boolean {
        readWriteLock.write {
            return super.addEntry(e)
        }
    }

    override fun removeFirst(): Boolean {
        readWriteLock.write {
            return super.removeFirst()
        }
    }

    fun setDrawLock()
    {
        readWriteLock.readLock().lock()
    }

    fun releaseLock()
    {
        readWriteLock.readLock().unlock()
    }

}

class ChartDataSetManager {

    private val mLivePositionSet = ThreadSafeLineDataSet(ArrayList<Entry>(100), "Feedback Position (%)")
    private val mLivePercentageSet = ThreadSafeLineDataSet(ArrayList<Entry>(100), "Command Position (%)")

    private val mLiveAccelXSet = ThreadSafeLineDataSet(ArrayList<Entry>(100), "Accel X (g)")
    private val mLiveAccelYSet = ThreadSafeLineDataSet(ArrayList<Entry>(100), "Accel Y (g)")
    private val mLiveAccelZSet = ThreadSafeLineDataSet(ArrayList<Entry>(100), "Accel Z (g)")

    //private val mLiveForceSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Force")
    private val mLiveTempSet = ThreadSafeLineDataSet(ArrayList<Entry>(100), "Temperature")

    private val mLiveData = arrayOf(mLivePositionSet, mLivePercentageSet, mLiveAccelXSet, mLiveAccelYSet, mLiveAccelZSet, mLiveTempSet)


    private val m5MinPositionSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Feedback Position (%)")
    private val m5MinPercentageSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Command Position (%)")

    private val m5MinAccelXSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Accel X (g)")
    private val m5MinAccelYSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Accel Y (g)")
    private val m5MinAccelZSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Accel Z (g)")

    //private val m5MinForceSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Force")
    private val m5MinTempSet = ThreadSafeLineDataSet(ArrayList<Entry>(), "Temperature")


    private var mLoadedData = arrayOf(m5MinPositionSet, m5MinPercentageSet, m5MinAccelXSet, m5MinAccelYSet, m5MinAccelZSet, m5MinTempSet)
    private var mLoadedDataName = ""

    var liveData = true

    val creationEpoch = System.currentTimeMillis()

    var loadedDataEpoch: Long = 0;

    init {

    }

    fun clearLiveData()
    {
        mLivePercentageSet.clear()
        mLivePositionSet.clear()
        mLiveAccelXSet.clear()
        mLiveAccelYSet.clear()
        mLiveAccelZSet.clear()
        mLiveTempSet.clear()
    }

    fun addLivePosition(position: Byte)
    {
        mLivePositionSet.addEntry(Entry((System.currentTimeMillis() - this.creationEpoch).toFloat() / 1000.0f, position.toFloat()))

        while(mLivePositionSet.entryCount > 100)
        {
            mLivePositionSet.removeFirst()
        }
    }

    fun addLivePercentage(percentage: Byte)
    {
        mLivePercentageSet.addEntry(Entry((System.currentTimeMillis() - this.creationEpoch).toFloat() / 1000.0f, percentage.toFloat()))

        while(mLivePercentageSet.entryCount > 100)
        {
            mLivePercentageSet.removeFirst()
        }
    }

    fun addLiveAccel(x: Float, y: Float, z: Float, force: Float, temp: Float)
    {
        val timeStamp = (System.currentTimeMillis() - this.creationEpoch).toFloat() / 1000.0f

        mLiveAccelXSet.addEntry(Entry(timeStamp, x))
        while(mLiveAccelXSet.entryCount > 100)
        {
            mLiveAccelXSet.removeFirst()
        }

        mLiveAccelYSet.addEntry(Entry(timeStamp, y))
        while(mLiveAccelYSet.entryCount > 100)
        {
            mLiveAccelYSet.removeFirst()
        }

        mLiveAccelZSet.addEntry(Entry(timeStamp, z))
        while(mLiveAccelZSet.entryCount > 100)
        {
            mLiveAccelZSet.removeFirst()
        }

//        mLiveForceSet.addEntry(Entry(timeStamp, force))
//        while(mLiveForceSet.entryCount > 100)
//        {
//            mLiveForceSet.removeFirst()
//        }

        mLiveTempSet.addEntry(Entry(timeStamp, temp))
        while(mLiveTempSet.entryCount > 100)
        {
            mLiveTempSet.removeFirst()
        }
    }

    fun getDataSet(): Array<ThreadSafeLineDataSet>?
    {
        return if(liveData)
        {
            mLiveData
        }
        else
        {
            mLoadedData
        }
    }

    fun getDataName(): String
    {
        return if(liveData) {
            "Live Chart"
        } else {
            mLoadedDataName
        }
    }

    fun loadDataFromFile(path: String)
    {
//        try {
//
//            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS")
//            val filePath = HydriveConnectApp.getContext().filesDir.path + "/DataRecordings/$path"
//            var isFahrenheit = false
//
//            File(filePath).useLines{ lines ->
//
//                m5MinPositionSet.clear()
//                m5MinPercentageSet.clear()
//                m5MinAccelXSet.clear()
//                m5MinAccelYSet.clear()
//                m5MinAccelZSet.clear()
//                m5MinForceSet.clear()
//                m5MinTempSet.clear()
//
//
//                val data = lines.toList()
//                var titleCount = 0
//                for( i in data.indices) {
//
//                    val line = data[i]
//
//                    val cleanString = line.trim { (it == '\r' || it == '\n') }
//
//                    val values = cleanString.split(',')
//
//                    if(i == 0) {
//                        titleCount = values.size
//                        for (value in values) {
//                            if (value == " Temperature(F)") {
//                                isFahrenheit = true
//                                continue
//                            }
//                        }
//                    }
//                    else
//                    {
//                        if(values.size < titleCount)
//                        {
//                            continue
//                        }
//
//                        val pointDate = LocalDateTime.parse(values[0], formatter)
//
//                        if(i == 1)
//                        {
//
//                            loadedDataEpoch = pointDate.atOffset(
//                                ZoneId.systemDefault().rules.getOffset(
//                                    Instant.now())).toInstant().toEpochMilli()
//                        }
//
//                        val pointTime = (pointDate.atOffset(
//                            ZoneId.systemDefault().rules.getOffset(
//                                Instant.now())).toInstant().toEpochMilli() - loadedDataEpoch).toFloat() / 1000.0f
//
//                        var cleanerValue = values[1].trim()
//                        m5MinPositionSet.addEntry(Entry(pointTime , cleanerValue.toFloat()))
//
//                        cleanerValue = values[2].trim()
//                        m5MinPercentageSet.addEntry(Entry(pointTime , cleanerValue.toFloat()))
//
//                        cleanerValue = values[3].trim()
//                        m5MinAccelXSet.addEntry(Entry(pointTime , cleanerValue.toFloat() * 0.00006103515625f))
//
//                        cleanerValue = values[4].trim()
//                        m5MinAccelYSet.addEntry(Entry(pointTime , cleanerValue.toFloat() * 0.00006103515625f))
//
//                        cleanerValue = values[5].trim()
//                        m5MinAccelZSet.addEntry(Entry(pointTime , cleanerValue.toFloat() * 0.00006103515625f))
//
//                        if(values.size > 6)
//                        {
//                            cleanerValue = values[6].trim()
//                            m5MinForceSet.addEntry(Entry(pointTime , cleanerValue.toFloat()))
//                        }
//
//                        if(values.size > 7)
//                        {
//                            cleanerValue = values[7].trim()
//
//                            var tempValue = cleanerValue.toFloat()
//
//                            if(!isFahrenheit && DataController.fahrenheit)
//                            {
//                                tempValue = ((tempValue * (9.0f / 5.0f)) + 32.0f)
//                            }
//                            else
//                            {
//                                tempValue = ((tempValue - 32.0f) * (5.0f / 9.0f))
//                            }
//
//                            m5MinTempSet.addEntry(Entry(pointTime, tempValue))
//                        }
//
//                    }
//                }
//
//            }
//            mLoadedDataName = path.removeSuffix("5Min.csv")
//            liveData = false
//        }
//        catch (e : Exception)
//        {
//            Log.d("Recording", "Failed to open file: " + e.message + e.toString())
//        }

    }

}