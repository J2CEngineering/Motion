package com.j2cengineering.motion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.j2cengineering.motion.cylinders.ConnectedDevice
import com.j2cengineering.motion.cylinders.Cylinder
import com.j2cengineering.motion.cylinders.CylinderList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val SETTINGS_PARAM1 = "cylinder_number"

/**
 * A simple [Fragment] subclass.
 * Use the [AlertsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlertsFragment : Fragment() {

    //private var cylinderNumber: Int = -1

    data class AlertValues(var date:String = "", var type:Int = 0, var info:String = "")

    inner class AlertsAdapter(private val alerts: ArrayList<AlertValues>): RecyclerView.Adapter<AlertsAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val dateTextView: TextView = itemView.findViewById<TextView>(R.id.alertDateTextView)
            val timeTextView: TextView = itemView.findViewById<TextView>(R.id.alertTimeTextView)
            val infoTextView: TextView = itemView.findViewById<TextView>(R.id.alertInfoTextView)
            val typeTextView: TextView = itemView.findViewById<TextView>(R.id.alertTypeTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val alertView = inflater.inflate(R.layout.alerts_list_view, parent, false)

            return ViewHolder(alertView)
        }

        override fun onBindViewHolder(holder: AlertsAdapter.ViewHolder, position: Int) {
            val alert: AlertValues = alerts[position]

            val dateAndTime = alert.date.split(" ")

            if(dateAndTime.count() >= 2)
            {
                holder.dateTextView.text = dateAndTime[0]
                holder.timeTextView.text = dateAndTime[1]
            }
            else
            {
                holder.dateTextView.text = ""
                holder.timeTextView.text = ""
            }

            holder.infoTextView.text = alert.info

            if(alert.type < alertTypeStrings.count()) {
                holder.typeTextView.text = alertTypeStrings[alert.type]
            }
            else{
                holder.typeTextView.text = "Unknown Alert: " + alert.type.toString()
            }
        }

        override fun getItemCount(): Int {
            return alerts.size
        }
    }

    private lateinit var progressLayout: LinearLayout
    private lateinit var progressTextView: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var alertRecyclerView : RecyclerView
    private lateinit var refreshButton: Button
    private lateinit var clearButton: Button

    private val alertArrayList = ArrayList<AlertValues>()
    private val alertsAdapter = AlertsAdapter(alertArrayList)
    private var alertDataCount = 0

    private lateinit var alertTypeStrings: Array<String>

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
        return inflater.inflate(R.layout.fragment_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alertTypeStrings = context?.resources?.getStringArray(R.array.AlertTypes) as Array<String>

        progressLayout = view.findViewById(R.id.alertProgressLayout)
        progressTextView = view.findViewById(R.id.alertProgessTextView)
        progressBar = view.findViewById(R.id.alertProgressBar)

        alertRecyclerView = view.findViewById(R.id.alertRecyclerView)
        refreshButton = view.findViewById(R.id.refreshAlertsButton)
        clearButton = view.findViewById(R.id.clearAlertsButton)

        refreshButton.setOnClickListener {
//            if(cylinderNumber >= 0 && CylinderList.getCylinder(cylinderNumber).connected && CylinderList.getCylinder(cylinderNumber).alertCount > 0) {
//                refreshButton.isEnabled = false
//                loadAlertsFromCylinder()
//            }

            if(ConnectedDevice.isConnected() && (ConnectedDevice.getCylinder()?.alertCount
                    ?: 0) > 0
            )
            {
                refreshButton.isEnabled = false
                loadAlertsFromCylinder()
            }
        }

        clearButton.setOnClickListener{
//            if(cylinderNumber >= 0)
//            {
                //val cylinder = CylinderList.getCylinder(cylinderNumber)
            val cylinder = ConnectedDevice.getCylinder()

            if(cylinder != null)
            {
                if(cylinder.connected) {
                    if (cylinder.loggedIn) {
                        cylinder.requestAlertInfo(-1)
                        progressTextView.text = "Clearing Alerts"
                        alertArrayList.clear()
                        alertsAdapter.notifyDataSetChanged()
                    } else {
//                        val dashboardData = Bundle().apply {
//                            putInt("cylinder_number", cylinderNumber)
//                        }
                        val logInDialog = LoginFragment()
//                        logInDialog.arguments = dashboardData

                        val ft = activity?.supportFragmentManager?.beginTransaction()
                        val prev = activity?.supportFragmentManager?.findFragmentByTag("login")

                        if (ft != null) {
                            if (prev != null) {
                                ft.remove(prev)
                            }

                            ft.addToBackStack(null)

                            logInDialog.show(ft, "login")
                        }
                    }
                }
            }
        }

        alertRecyclerView.adapter = alertsAdapter
        alertRecyclerView.layoutManager = LinearLayoutManager(this.context)

        alertRecyclerView.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
    }

    override fun onResume() {
        super.onResume()

//        if(cylinderNumber >= 0)
//        {
//            val cylinder = CylinderList.getCylinder(cylinderNumber)

        val cylinder = ConnectedDevice.getCylinder()
        if(cylinder != null)
        {
            cylinder.startBasicData()
            cylinder.setAlertChangeListener(alertListener)
            loadAlertsFromCylinder()
        }

    }

    override fun onPause() {
        super.onPause()

//        if(cylinderNumber >= 0)
//        {
//            val cylinder = CylinderList.getCylinder(cylinderNumber)

        val cylinder = ConnectedDevice.getCylinder()

        if(cylinder != null)
        {
            cylinder.setAlertChangeListener(null)
        }
    }

    private fun loadAlertsFromCylinder()
    {
//        if(cylinderNumber >= 0)
//        {
//            val cylinder = CylinderList.getCylinder(cylinderNumber)

        val cylinder = ConnectedDevice.getCylinder()

        if(cylinder != null)
        {
            if(cylinder.connected && cylinder.alertCount > 0)
            {
                alertArrayList.clear()
                alertDataCount = 0
                progressBar.max = cylinder.alertCount
                progressBar.progress = 0

                progressTextView.text = String.format(
                    "Downloading %d of %d.",
                    alertDataCount + 1,
                    cylinder.alertCount
                )
                alertsAdapter.notifyDataSetChanged()

                cylinder.requestAlertInfo(alertDataCount)
            }

        }
    }

    private val alertListener = object : Cylinder.OnAlertChangeListener{
        override fun OnAlertChange(cylinder: Cylinder, alertValues: Cylinder.AlertValues) {

            if(cylinder.alertCount > 0) {
                alertArrayList.add(
                    AlertValues(
                        alertValues.date,
                        alertValues.type,
                        alertValues.info
                    )
                )

                activity?.runOnUiThread {

                    progressBar.progress = alertDataCount + 1

                    progressTextView.text = String.format(
                        "Downloading %d of %d.",
                        alertDataCount + 1,
                        cylinder.alertCount
                    )
                    alertsAdapter.notifyItemChanged(alertDataCount)
                }
                alertDataCount++

                if (alertDataCount < cylinder.alertCount) {
                    cylinder.requestAlertInfo(alertDataCount)
                } else {
                    activity?.runOnUiThread {
                        progressTextView.text = "Download Complete"
                        refreshButton.isEnabled = true
                    }
                }
            }
            else
            {
                activity?.runOnUiThread {
                    refreshButton.isEnabled = true
                }
            }
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment AlertsFragment.
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