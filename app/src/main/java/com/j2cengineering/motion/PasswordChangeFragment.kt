package com.j2cengineering.motion

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.j2cengineering.motion.cylinders.ConnectedDevice
import com.j2cengineering.motion.cylinders.Cylinder
import com.j2cengineering.motion.cylinders.CylinderList
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val SETTINGS_PARAM1 = "cylinder_number"

/**
 * A simple [Fragment] subclass.
 * Use the [PasswordChangeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordChangeFragment : DialogFragment() {
    //private var cylinderNumber: Int = -1

    private lateinit var oldPasswordTextBox: EditText
    private lateinit var newPasswordTextBox: EditText

    private lateinit var showOldPasswordCheckBox: CheckBox
    private lateinit var showNewPasswordCheckBox: CheckBox

    private lateinit var passwordStatusTextView: TextView
    private lateinit var passwordStatusProgressBar: ProgressBar

    private lateinit var passwordCloseButton: Button
    private lateinit var passwordChangeButton: Button

    private var cylinder: Cylinder? = null

    private var changeTimer = Timer()
    private var changeTimerTask : TimerTask? = null



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
        return inflater.inflate(R.layout.fragment_password_change, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if(cylinderNumber >= 0)
//        {
//            cylinder = CylinderList.getCylinder(cylinderNumber)
//        }

        val cylinder = ConnectedDevice.getCylinder()

        cylinder?.setLogInListener(object : Cylinder.OnLogInListener{
            override fun OnLogInUpdate(cylinder: Cylinder, status: String) {

                activity?.runOnUiThread {
                    passwordChangeButton.isEnabled = true
                    passwordStatusTextView.text = status
                    passwordStatusProgressBar.visibility = View.GONE

                    changeTimer.cancel()
                }

                cylinder.startBasicData()
                cylinder.startTimeData()

            }
        })

        oldPasswordTextBox = view.findViewById(R.id.oldPasswordTextBox)
        newPasswordTextBox = view.findViewById(R.id.newPasswordTextBox)

        showOldPasswordCheckBox = view.findViewById(R.id.showOldPasswordCheckBox)
        showNewPasswordCheckBox = view.findViewById(R.id.showNewPasswordCheckBox)

        passwordStatusTextView = view.findViewById(R.id.textViewPasswordChangeStatus)
        passwordStatusProgressBar = view.findViewById(R.id.passwordChangeProgressBar)

        passwordCloseButton = view.findViewById(R.id.closePasswordButton)
        passwordChangeButton = view.findViewById(R.id.changePasswordButton)

        showOldPasswordCheckBox.setOnClickListener {
            if(showOldPasswordCheckBox.isChecked)
            {
                oldPasswordTextBox.transformationMethod = null
            }
            else
            {
                oldPasswordTextBox.transformationMethod = PasswordTransformationMethod()
            }
        }

        showNewPasswordCheckBox.setOnClickListener {
            if(showNewPasswordCheckBox.isChecked)
            {
                newPasswordTextBox.transformationMethod = null
            }
            else
            {
                newPasswordTextBox.transformationMethod = PasswordTransformationMethod()
            }
        }

        passwordCloseButton.setOnClickListener {
            dismiss()
        }

        passwordChangeButton.setOnClickListener {
            if(cylinder != null)
            {
                passwordChangeButton.isEnabled = false
                cylinder.stopBasicData()
                cylinder.stopTimeData()

                passwordStatusProgressBar.visibility = View.VISIBLE
                passwordStatusTextView.text = "Requesting Change..."

                cylinder.attemptPasswordChange(oldPasswordTextBox.text.toString(), newPasswordTextBox.text.toString())

                changeTimerTask = object : TimerTask()
                {
                    override fun run() {

                        cylinder.cancelPasswordChange()

                        activity?.runOnUiThread {
                            passwordChangeButton.isEnabled = true

                            passwordStatusTextView.text = "Change Request Timeout Please Retry"
                            passwordStatusProgressBar.visibility = View.GONE
                        }

                        cylinder.startBasicData()
                        cylinder.startTimeData()
                    }
                }

                changeTimer = Timer()
                changeTimer.schedule(changeTimerTask, 5000)

            }
        }

    }

    override fun dismiss() {
        super.dismiss()

        changeTimer.cancel()
        cylinder?.cancelPasswordChange()
        cylinder?.startBasicData()
        cylinder?.startTimeData()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment PasswordChangeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putInt(SETTINGS_PARAM1, param1)
                }
            }
    }
}