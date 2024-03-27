package com.j2cengineering.motion

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.utils.Utils
import com.j2cengineering.motion.controlUI.CylinderListInfoView
import com.j2cengineering.motion.cylinders.ConnectedDevice
import com.j2cengineering.motion.cylinders.Cylinder
import com.j2cengineering.motion.cylinders.CylinderList
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val DASHBOARD_PARAM1 = "cylinder_number"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var cylinderNumber: Int = -1

    private lateinit var nameTextBox: TextView
    private lateinit var addressTextBox: TextView
    private lateinit var setPositionTextBox: TextView
    private lateinit var currPositionTextBox: TextView
    private lateinit var topPressureTextBox: TextView
    private lateinit var bottomPressureTextBox: TextView
    private lateinit var temperatureTextBox: TextView
    private lateinit var cylinderInfoView: CylinderListInfoView
    private lateinit var cylinderWarning: ImageView
    private lateinit var alertsButton: Button
    private var greenStatus: Drawable? = null
    private var yellowStatus: Drawable? = null
    private var redStatus: Drawable? = null

    private lateinit var chartView: ThreadSafeLineChart

    private var currentChartData: LineData? = null
    private var currentChartDataSet: Array<ThreadSafeLineDataSet>? = null

    private var commandPositionSet: ThreadSafeLineDataSet? = null
    private var feedbackPositionSet: ThreadSafeLineDataSet? = null
    private var accelXSet: ThreadSafeLineDataSet? = null
    private var accelYSet: ThreadSafeLineDataSet? = null
    private var accelZSet: ThreadSafeLineDataSet? = null
    private var tempSet: ThreadSafeLineDataSet? = null

    private var updateTimer: Timer? = null
    private var chartDrawLock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cylinderNumber = it.getInt(DASHBOARD_PARAM1, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextBox = view.findViewById(R.id.name_textBox)
        addressTextBox = view.findViewById(R.id.address_textBox)
        setPositionTextBox = view.findViewById(R.id.set_position_textBox)
        currPositionTextBox = view.findViewById(R.id.current_position_textBox)
        topPressureTextBox = view.findViewById(R.id.top_pressure_textBox)
        bottomPressureTextBox = view.findViewById(R.id.bottom_pressure_textBox)
        temperatureTextBox = view.findViewById(R.id.temperature_textBox)
        cylinderInfoView = view.findViewById(R.id.cylinder_data_view)
        cylinderWarning = view.findViewById(R.id.alert_imageView)

        chartView = view.findViewById(R.id.chart_view)


        greenStatus = ResourcesCompat.getDrawable(resources, R.drawable.statusgreen, null)
        yellowStatus = ResourcesCompat.getDrawable(resources, R.drawable.statusyellow, null)
        redStatus = ResourcesCompat.getDrawable(resources, R.drawable.statusred, null)

        chartView.addListener(object : MyPlotListener{
            override fun OnBeforeDraw(chart: ThreadSafeLineChart, canvas: Canvas?) {
                commandPositionSet?.setDrawLock()
                feedbackPositionSet?.setDrawLock()
                accelXSet?.setDrawLock()
                accelYSet?.setDrawLock()
                accelZSet?.setDrawLock()
                //forceSet?.setDrawLock()
                tempSet?.setDrawLock()
            }

            override fun OnAfterDraw(chart: ThreadSafeLineChart, canvas: Canvas?) {
                commandPositionSet?.releaseLock()
                feedbackPositionSet?.releaseLock()
                accelXSet?.releaseLock()
                accelYSet?.releaseLock()
                accelZSet?.releaseLock()
                //forceSet?.releaseLock()
                tempSet?.releaseLock()
            }

            override fun OnBeforeNotifySetChange(chart: ThreadSafeLineChart) {
                commandPositionSet?.setDrawLock()
                feedbackPositionSet?.setDrawLock()
                accelXSet?.setDrawLock()
                accelYSet?.setDrawLock()
                accelZSet?.setDrawLock()
                //forceSet?.setDrawLock()
                tempSet?.setDrawLock()
            }

            override fun OnAfterNotifySetChange(chart: ThreadSafeLineChart) {
                commandPositionSet?.releaseLock()
                feedbackPositionSet?.releaseLock()
                accelXSet?.releaseLock()
                accelYSet?.releaseLock()
                accelZSet?.releaseLock()
                //forceSet?.releaseLock()
                tempSet?.releaseLock()
            }

        })


        chartView.legend.isWordWrapEnabled = true
        chartView.description.text = "Time in min:sec"


        val settingsButton = view.findViewById<Button>(R.id.settings_button)

        settingsButton.setOnClickListener {
            val dashboardData = Bundle().apply {
                putInt("cylinder_number", cylinderNumber)
            }
            findNavController().navigate(R.id.settingsFragment, dashboardData)
        }

        alertsButton = view.findViewById<Button>(R.id.alert_button)

        alertsButton.setOnClickListener {
            val dashboardData = Bundle().apply {
                putInt("cylinder_number", cylinderNumber)
            }
            findNavController().navigate(R.id.alertsFragment, dashboardData)
        }
    }

    override fun onResume() {
        super.onResume()

        //val cylinder = CylinderList.getCylinder(cylinderNumber)

        val cylinder = ConnectedDevice.getCylinder()


        if(cylinder != null) {
            cylinder.clearLiveChart()

            nameTextBox.text = cylinder.name
            addressTextBox.text = cylinder.address

            cylinderInfoView.percentage = cylinder.setPosition

            setPositionTextBox.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.setPosition)
                    ?: ""

            cylinderInfoView.position = cylinder.currPosition

            currPositionTextBox.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.currPosition)
                    ?: ""

            cylinderInfoView.isManual = cylinder.manualControl
            cylinderInfoView.isCalibrate = cylinder.calibrationMode
            cylinderInfoView.connected = cylinder.connected

            topPressureTextBox.text =
                view?.context?.getString(R.string.pressure_format_label, cylinder.pressure1.toInt())
                    ?: ""
            bottomPressureTextBox.text =
                view?.context?.getString(R.string.pressure_format_label, cylinder.pressure2.toInt())
                    ?: ""


            if (!ApplicationConfiguration.TemperatureUnitIsFahrenheit) {
                temperatureTextBox.text = view?.context?.getString(
                    R.string.temperature_celsius_format_label,
                    cylinder.temperature.toInt()
                ) ?: ""
            } else {
                temperatureTextBox.text = view?.context?.getString(
                    R.string.temperature_fahrenheit_format_label,
                    cylinder.temperature.toInt()
                ) ?: ""
            }

//        if(cylinder.alertCount > 0) {
//            cylinderWarning.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.alertsicon, null))
//        }
//        else
//        {
//            cylinderWarning.setImageDrawable(null)
//        }

            if (cylinder.alertCount > 0) {
                alertsButton.background = activity?.resources?.getDrawable(
                    R.drawable.alert_button_active_background,
                    null
                )
            } else {
                alertsButton.background = activity?.resources?.getDrawable(
                    R.drawable.settings_fragment_button_background,
                    null
                )
            }

            if (cylinder.connected) {
                if (cylinder.alertCount > 0) {
                    cylinderWarning.setImageDrawable(yellowStatus)
                } else {
                    cylinderWarning.setImageDrawable(greenStatus)
                }
            } else {
                cylinderWarning.setImageDrawable(redStatus)
            }
            //cylinderWarning.isVisible = cylinder.alertCount > 0

//            cylinder.setBasicDataListener(object : Cylinder.OnBasicDataListener {
//                override fun onConnectionChange(cylinder: Cylinder) {
//                    activity?.runOnUiThread {
//                        cylinderInfoView.connected = cylinder.connected
//
//                        if (cylinder.connected) {
//                            if (cylinder.alertCount > 0) {
//                                cylinderWarning.setImageDrawable(yellowStatus)
//                            } else {
//                                cylinderWarning.setImageDrawable(greenStatus)
//                            }
//                        } else {
//                            cylinderWarning.setImageDrawable(redStatus)
//                        }
//                    }
//                }
//
//                override fun onSetPositionChange(cylinder: Cylinder) {
//
//
//                    activity?.runOnUiThread {
//                        cylinderInfoView.percentage = cylinder.setPosition
//
//                        setPositionTextBox.text =
//                            view?.context?.getString(
//                                R.string.percentage_format_label,
//                                cylinder.setPosition
//                            )
//                                ?: ""
//                    }
//                }
//
//                override fun onCurrPositionChange(cylinder: Cylinder) {
//
//                    activity?.runOnUiThread {
//                        cylinderInfoView.position = cylinder.currPosition
//
//                        currPositionTextBox.text = view?.context?.getString(
//                            R.string.percentage_format_label,
//                            cylinder.currPosition
//                        ) ?: ""
//                    }
//                }
//
//                override fun onStatusChange(cylinder: Cylinder) {
//                    activity?.runOnUiThread {
//                        cylinderInfoView.isManual = cylinder.manualControl
//                        cylinderInfoView.isCalibrate = cylinder.calibrationMode
//                    }
//                }
//
//                override fun onAlertChange(cylinder: Cylinder) {
//                    cylinderWarning.post {
////                    if(cylinder.alertCount > 0) {
////                        cylinderWarning.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.alertsicon, null))
////                    }
////                    else
////                    {
////                        cylinderWarning.setImageDrawable(null)
////                    }
//
//                        if (cylinder.alertCount > 0) {
//                            alertsButton.background = activity?.resources?.getDrawable(
//                                R.drawable.alert_button_active_background,
//                                null
//                            )
//                        } else {
//                            alertsButton.background = activity?.resources?.getDrawable(
//                                R.drawable.settings_fragment_button_background,
//                                null
//                            )
//                        }
//
//                        if (cylinder.connected) {
//                            if (cylinder.alertCount > 0) {
//                                cylinderWarning.setImageDrawable(yellowStatus)
//                            } else {
//                                cylinderWarning.setImageDrawable(greenStatus)
//                            }
//                        } else {
//                            cylinderWarning.setImageDrawable(redStatus)
//                        }
//                    }
//
//                }
//
//            })
//
//
//            cylinder.setFullDataListener(object : Cylinder.OnFullDataListener {
//                override fun onPressureOneChange(cylinder: Cylinder) {
//                    activity?.runOnUiThread {
//                        topPressureTextBox.text = view?.context?.getString(
//                            R.string.pressure_format_label,
//                            cylinder.pressure1.toInt()
//                        ) ?: ""
//                    }
//                }
//
//                override fun onPressureTwoChange(cylinder: Cylinder) {
//                    activity?.runOnUiThread {
//                        bottomPressureTextBox.text = view?.context?.getString(
//                            R.string.pressure_format_label,
//                            cylinder.pressure2.toInt()
//                        ) ?: ""
//                    }
//                }
//
//                override fun onAccelChange(cylinder: Cylinder) {
//                    //TODO("Not yet implemented")
//                }
//
//                override fun onTemperatureChange(cylinder: Cylinder) {
//
//                    if (!ApplicationConfiguration.TemperatureUnitIsFahrenheit) {
//                        activity?.runOnUiThread {
//                            temperatureTextBox.text = view?.context?.getString(
//                                R.string.temperature_celsius_format_label,
//                                cylinder.temperature.toInt()
//                            ) ?: ""
//                        }
//                    } else {
//                        activity?.runOnUiThread {
//                            temperatureTextBox.text = view?.context?.getString(
//                                R.string.temperature_fahrenheit_format_label,
//                                cylinder.temperature.toInt()
//                            ) ?: ""
//                        }
//                    }
//                }
//
//                override fun onChartChange(cylinder: Cylinder) {
//                    //TODO("Not yet implemented")
//                }
//
//            })


            cylinder.startBasicData()
            cylinder.startFullData()


            cylinder.chartData.liveData = true



            chartView.clear()
            chartView.setMaxVisibleValueCount(0)
            chartView.setTouchEnabled(false)
            val xAxis = chartView.xAxis
            xAxis.valueFormatter = AxisTimeValueFormatter(cylinder.chartData)
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            currentChartData = LineData()


            currentChartDataSet = cylinder.chartData.getDataSet()


            commandPositionSet = currentChartDataSet?.get(1)
            feedbackPositionSet = currentChartDataSet?.get(0)
            accelXSet = currentChartDataSet?.get(2)
            accelYSet = currentChartDataSet?.get(3)
            accelZSet = currentChartDataSet?.get(4)
            //forceSet = currentChartDataSet?.get(5)
            tempSet = currentChartDataSet?.get(5)

            refreshVisibleCharts()

            var isSomeData = true

            if (currentChartData != null) {
                for (dataSet in currentChartData?.dataSets!!) {
                    if (dataSet.entryCount <= 1) {
                        isSomeData = false
                    }
                }
            }

            if (isSomeData) {
                if (cylinder.chartData.liveData) {
                    //chartView.setVisibleXRangeMaximum(20f)
                } else {
                    commandPositionSet?.entryCount?.let { chartView.setVisibleXRangeMaximum(it.toFloat()) }
                }
            }

            updateTimer = Timer()

            val timerTask = object : TimerTask() {
                override fun run() {
                    if (cylinder.connected && !chartDrawLock) {


//                    if(!cylinder.getIsDownloading())
//                    {

                        chartDrawLock = true
                        activity?.runOnUiThread {
                            if (cylinder.chartData.liveData) {
                                currentChartData!!.notifyDataChanged()
                                //chartView.invalidate()

                                var someData = false

                                if (currentChartData != null) {
                                    for (dataSet in currentChartData?.dataSets!!) {
                                        if (dataSet.entryCount >= 1) {
                                            someData = true
                                        }
                                    }
                                }

                                if (someData) {
                                    chartView.notifyDataSetChanged()
                                    //chartView.setVisibleXRangeMaximum(20f)

                                    if (chartView.data != null && chartView.data.entryCount > 0) {
                                        for (dataSet in chartView.data.dataSets) {

                                            if (dataSet.entryCount > 0) {
                                                chartView.moveViewToX(dataSet.xMax)
                                                break
                                            }
                                        }
                                    }
                                }
                            } else {
                                chartView.setTouchEnabled(true)
                            }
//                    }
//                    else
//                    {
//                        chartDownloadProgressBar.max = DataController.getTotalFrames()
//                        chartDownloadProgressBar.progress = DataController.getCurrentFrame()
//
//                        chartDownloadTextView.text = String.format("Downloading data: %d of %d", DataController.getCurrentFrame(), DataController.getTotalFrames())
//                    }
//
//                    if(DataController.chartDownloadComplete && chartDownloadTextView.text != "Download Complete")
//                    {
//                        activity?.runOnUiThread {
//                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//                        }
//                        chartDownloadTextView.text = "Download Complete"
//                    }
                            //chartView.invalidate()

                            chartDrawLock = false
                        }
                    }
                }
            }
            //updateTimer?.schedule(timerTask, 10000)
            updateTimer?.scheduleAtFixedRate(timerTask, 100, 200)
        }
    }

    override fun onPause() {
        super.onPause()

        //val cylinder = CylinderList.getCylinder(cylinderNumber)

        val cylinder = ConnectedDevice.getCylinder()

        if(cylinder != null) {
//            cylinder.stopBasicData()
//            cylinder.stopFullData()
//            cylinder.setBasicDataListener(null)
//            cylinder.setFullDataListener(null)
        }

    }

    private fun refreshVisibleCharts()
    {

        var positionAddedLeft = false
        var positionAddedRight = false
        var accelAddedLeft = false
        var accelAddedRight = false

        var leftLabel = ""
        var rightLabel = ""

        if(currentChartData != null)
        {
            while(currentChartData!!.dataSetCount > 0) {
                currentChartData!!.removeDataSet(0)
            }

            //val sharedPreferences = context?.getSharedPreferences(context?.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

            var settingsValue = 0//sharedPreferences?.getInt(context?.getString(R.string.save_chart_command), 0)

            if(settingsValue != 2)
            {
                commandPositionSet!!.setDrawCircles(false)
                commandPositionSet!!.color = view?.context?.getColor(R.color.position_chart)!!

                commandPositionSet!!.lineWidth = Utils.convertDpToPixel(0.5f)
                currentChartData!!.dataSets.add(commandPositionSet)

                if(settingsValue == 1)
                {
                    commandPositionSet!!.axisDependency = YAxis.AxisDependency.RIGHT
                    if(!positionAddedRight) {
                        if (rightLabel != "") {
                            rightLabel += "\r\n"
                        }
                        rightLabel += "Position (%)"
                        positionAddedRight = true
                    }
                }
                else {
                    commandPositionSet!!.axisDependency = YAxis.AxisDependency.LEFT

                    if(!positionAddedLeft) {
                        if (leftLabel != "") {
                            leftLabel += "\r\n"
                        }
                        leftLabel += "Position (%)"
                        positionAddedLeft = true
                    }
                }
            }

            settingsValue = 0 //sharedPreferences?.getInt(context?.getString(R.string.save_chart_feedback), 0)

            if(settingsValue != 2) {
                feedbackPositionSet!!.setDrawCircles(false)
                feedbackPositionSet!!.color = view?.context?.getColor(R.color.percentage_chart)!!
                feedbackPositionSet!!.lineWidth = Utils.convertDpToPixel(0.5f)
                currentChartData!!.dataSets.add(feedbackPositionSet)

                if(settingsValue == 1)
                {
                    feedbackPositionSet!!.axisDependency = YAxis.AxisDependency.RIGHT

                    if(!positionAddedRight) {
                        if (rightLabel != "") {
                            rightLabel += "\r\n"
                        }
                        rightLabel += "Position (%)"
                    }
                }
                else {
                    feedbackPositionSet!!.axisDependency = YAxis.AxisDependency.LEFT
                    if(!positionAddedLeft) {
                        if (leftLabel != "") {
                            leftLabel += "\r\n"
                        }
                        leftLabel += "Position (%)"
                    }
                }
            }

//            settingsValue = 2 //sharedPreferences?.getInt(context?.getString(R.string.save_chart_force), 0)
//
//            if(settingsValue != 2) {
//                forceSet!!.setDrawCircles(false)
//                forceSet!!.color = this.context?.getColor(R.color.force_chart)!!
//                forceSet!!.axisDependency = YAxis.AxisDependency.LEFT
//                forceSet!!.lineWidth = Utils.convertDpToPixel(0.5f)
//                forceSet!!.enableDashedLine((2.0f), (3.0f), 0.0f)
//                currentChartData!!.dataSets.add(forceSet)
//
//                if(settingsValue == 1)
//                {
//                    forceSet!!.axisDependency = YAxis.AxisDependency.RIGHT
//                    if (rightLabel != "") {
//                        rightLabel += "\r\n"
//                    }
//                    rightLabel += "Force (lbs)"
//                }
//                else {
//                    forceSet!!.axisDependency = YAxis.AxisDependency.LEFT
//                    if (leftLabel != "") {
//                        leftLabel += "\r\n"
//                    }
//                    leftLabel += "Force (lbs)"
//                }
//            }

            settingsValue = 0 //sharedPreferences?.getInt(context?.getString(R.string.save_chart_temp), 0)

            if(settingsValue != 2) {
                tempSet!!.setDrawCircles(false)
                tempSet!!.color = view?.context?.getColor(R.color.temperature_chart)!!
                tempSet!!.lineWidth = Utils.convertDpToPixel(0.5f)
                tempSet!!.enableDashedLine((2.0f), (3.0f), 0.0f)
                currentChartData!!.dataSets.add(tempSet)

                if(settingsValue == 1)
                {
                    tempSet!!.axisDependency = YAxis.AxisDependency.RIGHT

                    if (rightLabel != "") {
                        rightLabel += "\r\n"
                    }

//                    rightLabel += if(DataController.fahrenheit) {
//                        "Temperature (F)"
//                    } else {
//                        "Temperature (C)"
//                    }
                }
                else {
                    tempSet!!.axisDependency = YAxis.AxisDependency.LEFT

                    if (leftLabel != "") {
                        leftLabel += "\r\n"
                    }

//                    leftLabel += if(DataController.fahrenheit) {
//                        "Temperature (F)"
//                    } else {
//                        "Temperature (C)"
//                    }
                }
            }

            settingsValue = 1 //sharedPreferences?.getInt(context?.getString(R.string.save_chart_accelx), 1)

            if(settingsValue != 2) {
                accelXSet!!.setDrawCircles(false)
                accelXSet!!.color = view?.context?.getColor(R.color.accel_x_chart)!!
                accelXSet!!.lineWidth = Utils.convertDpToPixel(0.5f)
                currentChartData!!.dataSets.add(accelXSet)

                if(settingsValue == 1)
                {
                    accelXSet!!.axisDependency = YAxis.AxisDependency.RIGHT

                    if(!accelAddedRight) {
                        if (rightLabel != "") {
                            rightLabel += "\r\n"
                        }
                        rightLabel += "Acceleration (g)"
                        accelAddedRight = true
                    }

                }
                else {
                    accelXSet!!.axisDependency = YAxis.AxisDependency.LEFT

                    if(!accelAddedLeft) {
                        if (leftLabel != "") {
                            leftLabel += "\r\n"
                        }
                        leftLabel += "Acceleration (g)"
                        accelAddedLeft = true
                    }
                }
            }

            settingsValue = 1 //sharedPreferences?.getInt(context?.getString(R.string.save_chart_accely), 1)

            if(settingsValue != 2) {
                accelYSet!!.setDrawCircles(false)
                accelYSet!!.color = view?.context?.getColor(R.color.accel_y_chart)!!
                accelYSet!!.lineWidth = Utils.convertDpToPixel(0.5f)
                currentChartData!!.dataSets.add(accelYSet)

                if(settingsValue == 1)
                {
                    accelYSet!!.axisDependency = YAxis.AxisDependency.RIGHT

                    if(!accelAddedRight) {
                        if (rightLabel != "") {
                            rightLabel += "\r\n"
                        }
                        rightLabel += "Acceleration (g)"
                        accelAddedRight = true
                    }
                }
                else {
                    accelYSet!!.axisDependency = YAxis.AxisDependency.LEFT

                    if(!accelAddedLeft) {
                        if (leftLabel != "") {
                            leftLabel += "\r\n"
                        }
                        leftLabel += "Acceleration (g)"
                        accelAddedLeft = true
                    }
                }
            }

            settingsValue = 1 //sharedPreferences?.getInt(context?.getString(R.string.save_chart_accelz), 1)

            if(settingsValue != 2) {
                accelZSet!!.setDrawCircles(false)
                accelZSet!!.color = view?.context?.getColor(R.color.accel_z_chart)!!
                accelZSet!!.lineWidth = Utils.convertDpToPixel(0.5f)
                currentChartData!!.dataSets.add(accelZSet)

                if(settingsValue == 1)
                {
                    accelZSet!!.axisDependency = YAxis.AxisDependency.RIGHT

                    if(!accelAddedRight) {
                        if (rightLabel != "") {
                            rightLabel += "\r\n"
                        }
                        rightLabel += "Acceleration (g)"
                    }
                }
                else {
                    accelZSet!!.axisDependency = YAxis.AxisDependency.LEFT

                    if(!accelAddedLeft) {
                        if (leftLabel != "") {
                            leftLabel += "\r\n"
                        }
                        leftLabel += "Acceleration (g)"
                    }
                }
            }


            //leftUnitsTextView.text = leftLabel
            //rightUnitsTextView.text = rightLabel

            currentChartData!!.notifyDataChanged()
            chartView.data = currentChartData
//            if(currentChartData!!.entryCount > 0)
//            {
//
//            }
//            else
//            {
//                chartView.data = null
//            }

            chartView.notifyDataSetChanged()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putInt(DASHBOARD_PARAM1, param1)
                }
            }
    }
}