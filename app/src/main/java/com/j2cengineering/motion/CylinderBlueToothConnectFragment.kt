package com.j2cengineering.motion

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.j2cengineering.motion.cylinders.BluetoothCylinder
import com.j2cengineering.motion.cylinders.CylinderList


/**
 * A simple [Fragment] subclass.
 * Use the [CylinderBlueToothConnectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CylinderBlueToothConnectFragment : Fragment() {

    private lateinit var deviceList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cylinder_blue_tooth_connect, container, false)



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BluetoothConnection.setDeviceScanningListener(object : BluetoothConnection.OnDeviceScanningListener{
            override fun onDeviceFound(device: BluetoothDevice, rssi: Int, record: ByteArray) {

                val newDevice = BluetoothDeviceStorage(device)

                if(newDevice !in devices) {
                    devices.add(newDevice)
                    deviceAdapter.notifyItemInserted(devices.count() - 1)
                }
            }

            override fun onScanStop() {
                //TODO("Not yet implemented")
            }

            override fun onScanStart() {
                //TODO("Not yet implemented")
            }
        })


        deviceList = view.findViewById(R.id.device_list)
        deviceList.adapter = deviceAdapter
        deviceList.layoutManager = LinearLayoutManager(this.context)
        deviceList.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))

    }

    override fun onResume() {
        super.onResume()

        BluetoothConnection.startScanning()
    }

    override fun onPause() {
        super.onPause()

        BluetoothConnection.stopScanning()
    }

    data class BluetoothDeviceStorage(var device:BluetoothDevice? = null)
    {
        override fun equals(other: Any?): Boolean {
            var areSame = false

            if(other != null && other is BluetoothDeviceStorage)
            {
                areSame = this.device == other.device
            }

            return areSame
        }

        override fun hashCode(): Int {
            return device?.hashCode() ?: 0
        }
    }

    private val devices = ArrayList<BluetoothDeviceStorage>()
    private val deviceAdapter = BluetoothDeviceScanListAdapter(devices)

    private inner class BluetoothDeviceScanListAdapter(private val devices: ArrayList<BluetoothDeviceStorage>) : RecyclerView.Adapter<BluetoothDeviceScanListAdapter.ViewHolder>(){

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
            val device:BluetoothDeviceStorage = devices[position]

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


                val saveDialog = AlertDialog.Builder(context)

                saveDialog.setMessage("Do you want to save this connection?")
                saveDialog.setPositiveButton("Yes", DialogInterface.OnClickListener{
                        dialog, _ ->

                    dialog.dismiss()
                    val setNameDialog = SetNameFragment()

                    setNameDialog.mOnInputListener = object : SetNameFragment.OnInputListener{
                        override fun sendInput(input: String) {
                            CylinderList.addNewBluetoothCylinder(context!!, device.device?.address ?: "", input, true)

                            CylinderList.saveCylinderList(context!!)

                            findNavController().navigateUp()
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
                })

                saveDialog.setNegativeButton("No", DialogInterface.OnClickListener{
                    dialog, id ->

                    dialog.cancel()

                    CylinderList.addNewBluetoothCylinder(context!!, device.device?.address ?: "")


                    findNavController().navigateUp()

                })

                saveDialog.setCancelable(true)

                val save = saveDialog.create()

                save.setTitle("Save new connection")

                save.show()

            }
        }

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CylinderBlueToothConnectFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            CylinderBlueToothConnectFragment().apply {
            }
    }
}