package com.j2cengineering.motion.tabbedUI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.j2cengineering.motion.R
import com.j2cengineering.motion.cylinders.ConnectedDevice
import com.j2cengineering.motion.cylinders.Cylinder

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [TabbedCalibrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TabbedCalibrationFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var setPositionTextView: TextView
    private lateinit var currentPositionTextView: TextView
    private lateinit var highPositionTextView: TextView
    private lateinit var lowPositionTextView: TextView

    private lateinit var manualControlButton: Button
    private lateinit var plusOneButton: Button
    private lateinit var minusOneButton: Button

    private lateinit var calibrationButton: Button
    private lateinit var setHighButton: Button
    private lateinit var setLowButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tabbed_calibration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPositionTextView = view.findViewById(R.id.settings_set_position)
        currentPositionTextView = view.findViewById(R.id.settings_current_position)
        highPositionTextView = view.findViewById(R.id.settings_high_postion_textbox)
        lowPositionTextView = view.findViewById(R.id.settings_low_position_textbox)
        manualControlButton = view.findViewById(R.id.manual_control_button)
        plusOneButton = view.findViewById(R.id.plus_one_button)
        minusOneButton = view.findViewById(R.id.minus_one_button)
        calibrationButton = view.findViewById(R.id.calibration_button)
        setHighButton = view.findViewById(R.id.set_high_button)
        setLowButton = view.findViewById(R.id.set_low_button)

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



    }

    override fun onResume() {
        super.onResume()

//        val cylinder = CylinderList.getCylinder(cylinderNumber)
        val cylinder = ConnectedDevice.getCylinder()
        if(cylinder != null) {


            setPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.setPosition)
                    ?: ""


            currentPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.currPosition)
                    ?: ""

            highPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.calHigh) ?: ""


            lowPositionTextView.text =
                view?.context?.getString(R.string.percentage_format_label, cylinder.calLow) ?: ""



            cylinder.setBasicDataListener(dataListener)


            if (cylinder.loggedIn) {

                activity?.runOnUiThread {

                    manualControlButton.isEnabled = true
                    calibrationButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    manualControlButton.isEnabled = false
                    calibrationButton.isEnabled = false
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
                    plusOneButton.isEnabled = true
                    minusOneButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    manualControlButton.text =
                        activity?.resources?.getString(R.string.settings_set_manual_control)
                    manualControlButton.background = activity?.resources?.getDrawable(
                        R.drawable.settings_fragment_button_background,
                        null
                    )
                    plusOneButton.isEnabled = false
                    minusOneButton.isEnabled = false
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
                //cylinderInfoView.connected = cylinder.connected
            }
        }

        override fun onSetPositionChange(cylinder: Cylinder) {

            activity?.runOnUiThread {
                //cylinderInfoView.percentage = cylinder.setPosition

                setPositionTextView.text =
                    view?.context?.getString(R.string.percentage_format_label, cylinder.setPosition)
                        ?: ""
            }
        }

        override fun onCurrPositionChange(cylinder: Cylinder) {

            activity?.runOnUiThread {
                //cylinderInfoView.position = cylinder.realPosition

                currentPositionTextView.text = view?.context?.getString(
                    R.string.percentage_format_label,
                    cylinder.currPosition
                ) ?: ""

                highPositionTextView.text =
                    view?.context?.getString(R.string.percentage_format_label, cylinder.calHigh)
                        ?: ""

                //cylinderInfoView.setHighPosition = cylinder.calHigh

                lowPositionTextView.text =
                    view?.context?.getString(R.string.percentage_format_label, cylinder.calLow)
                        ?: ""

                //cylinderInfoView.setLowPosition = cylinder.calLow
            }
        }

        override fun onStatusChange(cylinder: Cylinder) {
            if (cylinder.loggedIn) {
                activity?.runOnUiThread {
                    manualControlButton.isEnabled = true
                    calibrationButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    manualControlButton.isEnabled = false
                    calibrationButton.isEnabled = false
                }
            }

            if (cylinder.manualControl) {
                activity?.runOnUiThread {
                    manualControlButton.text =
                        activity?.resources?.getString(R.string.settings_clear_manual_control)
                    manualControlButton.background = activity?.resources?.getDrawable(R.drawable.manual_button_activebackground, null)
                    plusOneButton.isEnabled = true
                    minusOneButton.isEnabled = true
                }
            } else {
                activity?.runOnUiThread {
                    manualControlButton.text =
                        activity?.resources?.getString(R.string.settings_set_manual_control)
                    manualControlButton.background = activity?.resources?.getDrawable(R.drawable.settings_fragment_button_background, null)
                    plusOneButton.isEnabled = false
                    minusOneButton.isEnabled = false
                }
            }

            if(cylinder.calibrationMode)
            {
                activity?.runOnUiThread {
                    calibrationButton.text = activity?.resources?.getString(R.string.settings_clear_calibration_mode)
                    calibrationButton.background = activity?.resources?.getDrawable(R.drawable.calibration_button_active_background, null)
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
                    setHighButton.isEnabled = false
                    setLowButton.isEnabled = false
                }
            }
        }

        override fun onAlertChange(cylinder: Cylinder) {
            //TODO("Not yet implemented")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TabbedCalibrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TabbedCalibrationFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}