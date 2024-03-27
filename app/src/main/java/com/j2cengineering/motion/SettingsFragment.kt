package com.j2cengineering.motion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.j2cengineering.motion.controlUI.CylinderListInfoView
import com.j2cengineering.motion.cylinders.ConnectedDevice
import com.j2cengineering.motion.cylinders.Cylinder
import com.j2cengineering.motion.cylinders.CylinderList
import java.time.LocalDateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val SETTINGS_PARAM1 = "cylinder_number"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    //private var cylinderNumber: Int = -1

    private lateinit var nameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var setPositionTextView: TextView
    private lateinit var currentPositionTextView: TextView
    private lateinit var highPositionTextView: TextView
    private lateinit var lowPositionTextView: TextView
    private lateinit var systemTimeTextView: TextView

    private lateinit var loginButton: Button
    private lateinit var renameButton: Button
    private lateinit var manualControlButton: Button
    private lateinit var plusOneButton: Button
    private lateinit var plusFiveButton: Button
    private lateinit var minusFiveButton: Button
    private lateinit var minusOneButton: Button
    private lateinit var calibrationButton: Button
    private lateinit var setHighButton: Button
    private lateinit var setLowButton: Button
    private lateinit var setTimeButton: Button
    private lateinit var startPurgeButton: Button
    private lateinit var setTemperatureButton: Button
    private lateinit var changePasswordButton: Button

    private lateinit var cylinderInfoView: CylinderListInfoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            cylinderNumber = it.getInt(SETTINGS_PARAM1)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextView = view.findViewById(R.id.settings_name_textBox)
        addressTextView = view.findViewById(R.id.settings_address_textBox)
        setPositionTextView = view.findViewById(R.id.settings_set_position)
        currentPositionTextView = view.findViewById(R.id.settings_current_position)
        highPositionTextView = view.findViewById(R.id.settings_high_postion_textbox)
        lowPositionTextView = view.findViewById(R.id.settings_low_position_textbox)
        systemTimeTextView = view.findViewById(R.id.settings_time_textbox)


        cylinderInfoView = view.findViewById(R.id.settings_cylinder_view)

        loginButton = view.findViewById(R.id.login_button)
        renameButton = view.findViewById(R.id.rename_button)
        manualControlButton = view.findViewById(R.id.manual_control_button)
        plusOneButton = view.findViewById(R.id.plus_one_button)
        plusFiveButton = view.findViewById(R.id.plus_five_button)
        minusFiveButton = view.findViewById(R.id.minus_five_button)
        minusOneButton = view.findViewById(R.id.minus_one_button)
        calibrationButton = view.findViewById(R.id.calibration_button)
        setHighButton = view.findViewById(R.id.set_high_button)
        setLowButton = view.findViewById(R.id.set_low_button)
        setTimeButton = view.findViewById(R.id.set_time_button)
        startPurgeButton = view.findViewById(R.id.manual_purge_button)
        setTemperatureButton = view.findViewById(R.id.set_temperature_button)
        changePasswordButton = view.findViewById(R.id.change_password_button)

        loginButton.setOnClickListener {


            //val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()

            if(cylinder != null) {
                if (!cylinder.loggedIn) {
//                    val dashboardData = Bundle().apply {
//                        putInt("cylinder_number", cylinderNumber)
//                    }
                    val logInDialog = LoginFragment()
//                    logInDialog.arguments = dashboardData

                    val ft = activity?.supportFragmentManager?.beginTransaction()
                    val prev = activity?.supportFragmentManager?.findFragmentByTag("login")

                    if (ft != null) {
                        if (prev != null) {
                            ft.remove(prev)
                        }

                        ft.addToBackStack(null)

                        logInDialog.show(ft, "login")
                    }
                } else {
                    cylinder.sendLogout()
                }
            }

        }

        renameButton.setOnClickListener {
            val setNameDialog = SetNameFragment()

            setNameDialog.mOnInputListener = object : SetNameFragment.OnInputListener{
                override fun sendInput(input: String) {
                    //val cylinder = CylinderList.getCylinder(cylinderNumber)
                    val cylinder = ConnectedDevice.getCylinder()
                    if(cylinder != null) {
                        cylinder.name = input

                        nameTextView.post {
                            nameTextView.text = input
                        }
                    }

                }
            }

            val ft = activity?.supportFragmentManager?.beginTransaction()
            val prev = activity?.supportFragmentManager?.findFragmentByTag("name")

            if (ft != null) {
                if (prev != null) {
                    ft.remove(prev)
                }

                ft.addToBackStack(null)

                setNameDialog.show(ft, "name")
            }
        }

        manualControlButton.setOnClickListener {
//            if(cylinderNumber >= 0) {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)
            val cylinder = ConnectedDevice.getCylinder()

            if(cylinder != null)
            {

                if(!cylinder.manualControl)
                {
                    cylinder.setManualControl()
                }
                else
                {
                    cylinder.setAutomaticControl()
                }
            }
        }

        plusOneButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(cylinder != null)
            {
                var newPercent = cylinder.setPosition + 1

                if(newPercent > 100)
                {
                    newPercent = 100
                }

                cylinder.setCylinderPercentage(newPercent)
            }
        }

        plusFiveButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(cylinder != null)
            {
                var newPercent = cylinder.setPosition + 5

                if(newPercent > 100)
                {
                    newPercent = 100
                }

                cylinder.setCylinderPercentage(newPercent)
            }
        }

        minusFiveButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(cylinder != null)
            {
                var newPercent = cylinder.setPosition - 5

                if(newPercent < 0)
                {
                    newPercent = 0
                }

                cylinder.setCylinderPercentage(newPercent)
            }
        }

        minusOneButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(cylinder != null)
            {
                var newPercent = cylinder.setPosition - 1

                if(newPercent < 0)
                {
                    newPercent = 0
                }

                cylinder.setCylinderPercentage(newPercent)
            }
        }

        calibrationButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)
            ConnectedDevice.getCylinder()?.toggleCalibrationMode()
        }

        setHighButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            ConnectedDevice.getCylinder()?.setCalibrationHigh()
        }

        setLowButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(cylinder != null)
            {
                cylinder.setCalibrationLow()
            }
        }

        setTimeButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(cylinder != null)
            {
                cylinder.writeDeviceTime(LocalDateTime.now())
            }
        }

        startPurgeButton.setOnClickListener {
//            if(cylinderNumber >= 0)
//            {
//                val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(cylinder != null)
            {
                cylinder.triggerPurge()
            }
        }

        changePasswordButton.setOnClickListener {
            //val cylinder = CylinderList.getCylinder(cylinderNumber)

            val cylinder = ConnectedDevice.getCylinder()
            if(ConnectedDevice.isConnected()) {
//                val dashboardData = Bundle().apply {
//                    putInt("cylinder_number", cylinderNumber)
//                }
                val logInDialog = PasswordChangeFragment()
                //logInDialog.arguments = dashboardData

                val ft = activity?.supportFragmentManager?.beginTransaction()
                val prev = activity?.supportFragmentManager?.findFragmentByTag("password")

                if (ft != null) {
                    if (prev != null) {
                        ft.remove(prev)
                    }

                    ft.addToBackStack(null)

                    logInDialog.show(ft, "password")
                }
            }
        }

        setTemperatureButton.setOnClickListener {
            //val cylinder = CylinderList.getCylinder(cylinderNumber)

            val logInDialog = TemperatureUnitSelectFragment()

            val ft = activity?.supportFragmentManager?.beginTransaction()
            val prev = activity?.supportFragmentManager?.findFragmentByTag("temperature")

            if (ft != null) {
                if (prev != null) {
                    ft.remove(prev)
                }

                ft.addToBackStack(null)

                logInDialog.show(ft, "temperature")
            }
        }

    }

    override fun onResume() {
        super.onResume()

//        val cylinder = CylinderList.getCylinder(cylinderNumber)
        val cylinder = ConnectedDevice.getCylinder()
        if(cylinder != null) {
            nameTextView.text = cylinder.name
            addressTextView.text = cylinder.address

            cylinderInfoView.connected = cylinder.connected

            cylinderInfoView.percentage = cylinder.setPosition

            setPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.setPosition)
                    ?: ""

            cylinderInfoView.position = cylinder.realPosition

            currentPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.currPosition)
                    ?: ""

            highPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.calHigh) ?: ""

            cylinderInfoView.setHighPosition = cylinder.calHigh

            lowPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.calLow) ?: ""

            cylinderInfoView.setLowPosition = cylinder.calLow

            systemTimeTextView.text = view?.context?.getString(
                R.string.settings_time_string,
                cylinder.cylinderTime.day,
                cylinder.cylinderTime.month,
                cylinder.cylinderTime.year,
                cylinder.cylinderTime.hour,
                cylinder.cylinderTime.minute,
                cylinder.cylinderTime.second
            )


            cylinder.setBasicDataListener(dataListener)

            cylinder.setTimeDataListener(timeListener)

            if (cylinder.loggedIn) {

                activity?.runOnUiThread {
                    loginButton.text =
                        activity?.resources?.getString(R.string.settings_log_out_label)

                    manualControlButton.isEnabled = true
                    calibrationButton.isEnabled = true
                    setTimeButton.isEnabled = true
                    changePasswordButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    loginButton.text =
                        activity?.resources?.getString(R.string.settings_log_in_label)
                    manualControlButton.isEnabled = false
                    calibrationButton.isEnabled = false
                    setTimeButton.isEnabled = false
                    changePasswordButton.isEnabled = false
                }
            }

            if (cylinder.manualControl) {
                activity?.runOnUiThread {
                    manualControlButton.text =
                        activity?.resources?.getString(R.string.settings_clear_manual_control)
                    manualControlButton.background = activity?.resources?.getDrawable(
                        R.drawable.manual_button_activebackground,
                        null
                    )
                    cylinderInfoView.isManual = true
                    plusOneButton.isEnabled = true
                    plusFiveButton.isEnabled = true
                    minusOneButton.isEnabled = true
                    minusFiveButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    manualControlButton.text =
                        activity?.resources?.getString(R.string.settings_set_manual_control)
                    manualControlButton.background = activity?.resources?.getDrawable(
                        R.drawable.settings_fragment_button_background,
                        null
                    )
                    cylinderInfoView.isManual = false
                    plusOneButton.isEnabled = false
                    plusFiveButton.isEnabled = false
                    minusOneButton.isEnabled = false
                    minusFiveButton.isEnabled = false
                }
            }

            if (cylinder.calibrationMode) {
                activity?.runOnUiThread {
                    calibrationButton.text =
                        activity?.resources?.getString(R.string.settings_clear_calibration_mode)
                    calibrationButton.background = activity?.resources?.getDrawable(
                        R.drawable.calibration_button_active_background,
                        null
                    )
                    cylinderInfoView.isCalibrate = true
                    setHighButton.isEnabled = true
                    setLowButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    calibrationButton.text =
                        activity?.resources?.getString(R.string.settings_set_calibration_mode)
                    calibrationButton.background = activity?.resources?.getDrawable(
                        R.drawable.settings_fragment_button_background,
                        null
                    )
                    cylinderInfoView.isCalibrate = false
                    setHighButton.isEnabled = false
                    setLowButton.isEnabled = false
                }
            }

            cylinder.startBasicData()
            cylinder.startTimeData()
        }
    }

    override fun onPause() {
        super.onPause()

        //val cylinder = CylinderList.getCylinder(cylinderNumber)
        val cylinder = ConnectedDevice.getCylinder()

        if(cylinder != null) {
            cylinder.stopBasicData()
            cylinder.stopTimeData()

            cylinder.setBasicDataListener(null)
            cylinder.setTimeDataListener(null)
        }
    }

    private val dataListener = object : Cylinder.OnBasicDataListener{
        override fun onConnectionChange(cylinder: Cylinder) {
            activity?.runOnUiThread {
                cylinderInfoView.connected = cylinder.connected
            }
        }

        override fun onSetPositionChange(cylinder: Cylinder) {

            activity?.runOnUiThread {
                cylinderInfoView.percentage = cylinder.setPosition

                setPositionTextView.text =
                    view?.context?.getString(R.string.percentage_format_label, cylinder.setPosition)
                        ?: ""
            }
        }

        override fun onCurrPositionChange(cylinder: Cylinder) {

            activity?.runOnUiThread {
                cylinderInfoView.position = cylinder.realPosition

                currentPositionTextView.text = view?.context?.getString(
                    R.string.percentage_format_label,
                    cylinder.currPosition
                ) ?: ""

                highPositionTextView.text =
                    view?.context?.getString(R.string.percentage_format_label, cylinder.calHigh)
                        ?: ""

                cylinderInfoView.setHighPosition = cylinder.calHigh

                lowPositionTextView.text =
                    view?.context?.getString(R.string.percentage_format_label, cylinder.calLow)
                        ?: ""

                cylinderInfoView.setLowPosition = cylinder.calLow
            }
        }

        override fun onStatusChange(cylinder: Cylinder) {
            if (cylinder.loggedIn) {
                activity?.runOnUiThread {
                    loginButton.text =
                        activity?.resources?.getString(R.string.settings_log_out_label)

                    manualControlButton.isEnabled = true
                    calibrationButton.isEnabled = true
                    setTimeButton.isEnabled = true
                    changePasswordButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    loginButton.text =
                        activity?.resources?.getString(R.string.settings_log_in_label)
                    manualControlButton.isEnabled = false
                    calibrationButton.isEnabled = false
                    setTimeButton.isEnabled = false
                    changePasswordButton.isEnabled = false
                }
            }

            if (cylinder.manualControl) {
                activity?.runOnUiThread {
                    manualControlButton.text =
                        activity?.resources?.getString(R.string.settings_clear_manual_control)
                    manualControlButton.background = activity?.resources?.getDrawable(R.drawable.manual_button_activebackground, null)
                    cylinderInfoView.isManual = true
                    plusOneButton.isEnabled = true
                    plusFiveButton.isEnabled = true
                    minusOneButton.isEnabled = true
                    minusFiveButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    manualControlButton.text =
                        activity?.resources?.getString(R.string.settings_set_manual_control)
                    manualControlButton.background = activity?.resources?.getDrawable(R.drawable.settings_fragment_button_background, null)
                    cylinderInfoView.isManual = false
                    plusOneButton.isEnabled = false
                    plusFiveButton.isEnabled = false
                    minusOneButton.isEnabled = false
                    minusFiveButton.isEnabled = false
                }
            }

            if(cylinder.calibrationMode)
            {
                activity?.runOnUiThread {
                    calibrationButton.text = activity?.resources?.getString(R.string.settings_clear_calibration_mode)
                    calibrationButton.background = activity?.resources?.getDrawable(R.drawable.calibration_button_active_background, null)
                    cylinderInfoView.isCalibrate = true
                    setHighButton.isEnabled = true
                    setLowButton.isEnabled = true
                }
            }
            else
            {
                activity?.runOnUiThread {
                    calibrationButton.text =
                        activity?.resources?.getString(R.string.settings_set_calibration_mode)
                    calibrationButton.background = activity?.resources?.getDrawable(R.drawable.settings_fragment_button_background, null)
                    cylinderInfoView.isCalibrate = false
                    setHighButton.isEnabled = false
                    setLowButton.isEnabled = false
                }
            }
        }

        override fun onAlertChange(cylinder: Cylinder) {
            //TODO("Not yet implemented")
        }
    }

    private val timeListener = object : Cylinder.OnTimeChangeListener {
        override fun onCylinderTimeChange(cylinder: Cylinder) {
            systemTimeTextView.text = view?.context?.getString(
                R.string.settings_time_string,
                cylinder.cylinderTime.day,
                cylinder.cylinderTime.month,
                cylinder.cylinderTime.year,
                cylinder.cylinderTime.hour,
                cylinder.cylinderTime.minute,
                cylinder.cylinderTime.second
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(SETTINGS_PARAM1, param1)
                }
            }
    }
}