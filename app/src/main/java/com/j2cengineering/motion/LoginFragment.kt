package com.j2cengineering.motion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.j2cengineering.motion.cylinders.ConnectedDevice
import com.j2cengineering.motion.cylinders.Cylinder
import com.j2cengineering.motion.cylinders.CylinderList
import java.util.*
import kotlin.math.log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val SETTINGS_PARAM1 = "cylinder_number"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : DialogFragment() {

    //private var cylinderNumber: Int = -1

    private lateinit var cancelButton: Button
    private lateinit var loginButton: Button

    private lateinit var progressBar: ProgressBar

    private lateinit var nameTextView: EditText
    private lateinit var passwordTextView: EditText
    private lateinit var logInStatusTextView: TextView

    private var logInTimer = Timer()
    private var loginTimerTask : TimerTask? = null

    private var cylinder: Cylinder? = null

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
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if(cylinderNumber >= 0)
//        {
//            cylinder = CylinderList.getCylinder(cylinderNumber)
//        }

        val cylinder = ConnectedDevice.getCylinder()

        cylinder?.setLogInListener(object : Cylinder.OnLogInListener {
            override fun OnLogInUpdate(cylinder: Cylinder, status: String) {

                activity?.runOnUiThread {
                    loginButton.isEnabled = true
                    logInStatusTextView.text = status
                    progressBar.visibility = View.GONE
                    logInTimer.cancel()

                    if(status == "Success!")
                    {
                        cylinder.username = nameTextView.text.toString()
                        cylinder.password = passwordTextView.text.toString()

                        CylinderList.saveCylinderList(context!!)
                    }


                }
                cylinder.startBasicData()
                cylinder.startTimeData()
            }

        })

        nameTextView = view.findViewById(R.id.editTextLoginUserName)
        passwordTextView = view.findViewById(R.id.editTextLoginPassword)
        logInStatusTextView = view.findViewById(R.id.textViewLoginStatus)

        progressBar = view.findViewById(R.id.loginProgressBar)

        cancelButton = view.findViewById(R.id.loginCancelButton)

        cancelButton.setOnClickListener {
            dismiss()
        }

        loginButton = view.findViewById(R.id.loginLoginButton)

        loginButton.setOnClickListener {

            if(cylinder != null) {

                loginButton.isEnabled = false
                cylinder?.stopBasicData()
                cylinder?.stopTimeData()

                progressBar.visibility = View.VISIBLE
                logInStatusTextView.text = "Logging In..."

                cylinder?.attemptLogIn(nameTextView.text.toString(), passwordTextView.text.toString())


                loginTimerTask = object : TimerTask(){
                    override fun run() {
                        cylinder?.cancelLogIn()

                        activity?.runOnUiThread {
                            loginButton.isEnabled = true
                            logInStatusTextView.text = "Login Timeout Please Retry"
                            progressBar.visibility = View.GONE
                        }
                        cylinder?.startBasicData()
                        cylinder?.startTimeData()
                    }

                }
                logInTimer = Timer()
                logInTimer.schedule(loginTimerTask, 5000)

            }

        }
    }

    override fun onResume() {
        super.onResume()

        nameTextView.setText(cylinder?.username ?: "")
        passwordTextView.setText(cylinder?.password ?: "")
    }

    override fun dismiss() {
        super.dismiss()

        logInTimer.cancel()
        cylinder?.cancelLogIn()
        cylinder?.startBasicData()
        cylinder?.startTimeData()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment LoginFragment.
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