package com.j2cengineering.motion

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.j2cengineering.motion.cylinders.ConnectedDevice
import com.j2cengineering.motion.cylinders.CylinderList

/**
 * A simple [Fragment] subclass.
 * Use the [CylinderConnectListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CylinderConnectListFragment : Fragment() {

    private lateinit var deviceList: RecyclerView
    private lateinit var scanningStatusTextView: TextView
    private lateinit var scanningButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cylinder_connect_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanningStatusTextView = view.findViewById(R.id.scanningStatusTextView)
        scanningButton = view.findViewById(R.id.scannerControlButton)

        BluetoothConnection.setDeviceScanningListener(object : BluetoothConnection.OnDeviceScanningListener{
            override fun onDeviceFound(device: BluetoothDevice, rssi: Int, record: ByteArray) {

                val newDevice = CylinderBlueToothConnectFragment.BluetoothDeviceStorage(device)

                if(newDevice !in devices) {
                    devices.add(newDevice)
                    deviceAdapter.notifyItemInserted(devices.count() - 1)
                }
            }

            override fun onScanStart() {
                scanningStatusTextView.text = "Scanning for Cylinders"
                scanningButton.text = "Stop Scanning"
            }

            override fun onScanStop() {
                scanningStatusTextView.text = ""
                scanningButton.text = "Start Scanning"
            }
        })


        scanningButton.setOnClickListener{
            if(BluetoothConnection.isScanning)
            {
                BluetoothConnection.stopScanning()
            }
            else
            {
                devices.clear()
                deviceAdapter.notifyDataSetChanged()
                BluetoothConnection.startScanning()
            }
        }

        deviceList = view.findViewById(R.id.connectionListrecyclerView)
        deviceList.adapter = deviceAdapter
        deviceList.layoutManager = LinearLayoutManager(this.context)
        deviceList.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))



    }

    override fun onResume() {
        super.onResume()

        ConnectedDevice.disconnectCylinder()
        devices.clear()
        deviceAdapter.notifyDataSetChanged()
        BluetoothConnection.startScanning()
    }

    override fun onPause() {
        super.onPause()

        BluetoothConnection.stopScanning()
    }

    private val devices = ArrayList<CylinderBlueToothConnectFragment.BluetoothDeviceStorage>()
    private val deviceAdapter = BluetoothDeviceScanListAdapter(devices)

    private inner class BluetoothDeviceScanListAdapter(private val devices: ArrayList<CylinderBlueToothConnectFragment.BluetoothDeviceStorage>) : RecyclerView.Adapter<BluetoothDeviceScanListAdapter.ViewHolder>(){

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val titleView: TextView = itemView.findViewById(R.id.broadcast_name)
            val subTitle: TextView = itemView.findViewById(R.id.content)
            val connectButton: Button = itemView.findViewById(R.id.cylinder_connect_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val alertView = inflater.inflate(R.layout.cylinder_bluetooth_listing_content, parent, false)

            return ViewHolder(alertView)
        }

        override fun getItemCount(): Int {
            return devices.count()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device: CylinderBlueToothConnectFragment.BluetoothDeviceStorage = devices[position]

            try {
                holder.titleView.text = getString(R.string.model_name).format(device.device?.name)

            }catch (e:SecurityException)
            {
                holder.titleView.text = getString(R.string.model_name).format("")
            }

            holder.subTitle.text = getString(R.string.serial_number).format(device.device?.address ?: "")

            holder.connectButton.setOnClickListener {
//                val newCylinder = BluetoothCylinder(device.device?.address ?: "")
//
//                newCylinder.connectCylinder(BluetoothConnection, context)


                val success = ConnectedDevice.connectBluetoothCylinder(device.device?.address ?: "", context!!)


                if(success)
                {
                    findNavController().navigate(R.id.dashboardFragment)
                }

            }

        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment CylinderConnectListFragment.
         */
        @JvmStatic
        fun newInstance() =
            CylinderConnectListFragment().apply {
            }
    }
}