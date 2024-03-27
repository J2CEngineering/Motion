package com.j2cengineering.motion.cylinders

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.j2cengineering.motion.BluetoothConnection
import com.j2cengineering.motion.R
import com.j2cengineering.motion.controlUI.CylinderListInfoView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths

object CylinderList {

    private val cylinderArrayList:ArrayList<Cylinder> = ArrayList()

    private val cylinderListAdapter = CylinderListAdapter(cylinderArrayList)



    fun getListAdapter(): CylinderListAdapter{
        return cylinderListAdapter
    }

    fun setAllBasicDataCallback(callback: Cylinder.OnBasicDataListener)
    {
        for(cylinder in cylinderArrayList)
        {
            cylinder.setBasicDataListener(callback)
        }
    }

    fun startAllBasicData()
    {
        for (cylinder in cylinderArrayList)
        {
            cylinder.startBasicData()
        }
    }

    fun stopAllBasicData()
    {
        for (cylinder in cylinderArrayList)
        {
            cylinder.stopBasicData()
        }
    }

    fun getCylinder(index:Int):Cylinder
    {
        return cylinderArrayList[index]
    }

    fun addNewBluetoothCylinder(context:Context, deviceAddress:String, name: String = "New Cylinder", saved: Boolean = false)
    {
        val newCylinder = BluetoothCylinder(deviceAddress, name, saved)
        newCylinder.connectCylinder(BluetoothConnection, context)

        cylinderArrayList.add(newCylinder)
        //cylinderListAdapter.notifyItemInserted(cylinderArrayList.count())
    }

    fun indexOf(cylinder: Cylinder): Int
    {
        return cylinderArrayList.indexOf(cylinder)
    }

    class CylinderListAdapter(private val cylinders:ArrayList<Cylinder>):RecyclerView.Adapter<CylinderListAdapter.ViewHolder>()
    {

        inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
        {
            val cylinderName:TextView = itemView.findViewById(R.id.cylinder_name_textbox)
            val cylinderAddress:TextView = itemView.findViewById(R.id.cylinder_address_textbox)
            val cylinderBasicData:CylinderListInfoView = itemView.findViewById(R.id.cylinder_basic_data_textbox)
            val cylinderWarning:ImageView = itemView.findViewById(R.id.cylinder_list_warning_image)
            val greenStatus: Drawable? = ResourcesCompat.getDrawable(itemView.resources, R.drawable.statusgreen, null)
            val yellowStatus: Drawable? = ResourcesCompat.getDrawable(itemView.resources, R.drawable.statusyellow, null)
            val redStatus: Drawable? = ResourcesCompat.getDrawable(itemView.resources, R.drawable.statusred, null)



            val cylinderUpdateCallback = object: Cylinder.OnBasicDataListener{
                override fun onConnectionChange(cylinder: Cylinder) {
                    //cylinderAddress.text = cylinder.address
                    cylinderBasicData.post {
                        cylinderBasicData.connected = cylinder.connected
                    }

                    cylinderWarning.post {
                        if (cylinder.connected) {
                            if (cylinder.alertCount > 0) {
                                cylinderWarning.setImageDrawable(yellowStatus)

                            } else {
                                cylinderWarning.setImageDrawable(greenStatus)
                            }
                        } else {
                            cylinderWarning.setImageDrawable(redStatus)
                        }
                    }
                }

                override fun onSetPositionChange(cylinder: Cylinder) {
                    cylinderBasicData.post {
                        cylinderBasicData.percentage = cylinder.setPosition
                    }
                    //cylinderBasicData.invalidate()
                }

                override fun onCurrPositionChange(cylinder: Cylinder) {
                    cylinderBasicData.post {
                        cylinderBasicData.position = cylinder.currPosition
                    }
                    //cylinderBasicData.invalidate()
                }

                override fun onStatusChange(cylinder: Cylinder) {
                    cylinderBasicData.post {
                        cylinderBasicData.isManual = cylinder.manualControl
                        cylinderBasicData.isCalibrate = cylinder.calibrationMode
                    }
                }

                override fun onAlertChange(cylinder: Cylinder) {
                    cylinderWarning.post {

//                        if(cylinder.alertCount > 0) {
//                            cylinderWarning.setImageDrawable(ResourcesCompat.getDrawable(itemView.resources, R.drawable.alertsicon, null))
//                        }
//                        else
//                        {
//                            cylinderWarning.setImageDrawable(null)
//                        }
                        //cylinderWarning.isVisible = cylinder.alertCount > 0
                        if(cylinder.connected) {
                            if (cylinder.alertCount > 0) {
                                cylinderWarning.setImageDrawable(yellowStatus)
                            } else {
                                cylinderWarning.setImageDrawable(greenStatus)
                            }
                        }
                        else
                        {
                            cylinderWarning.setImageDrawable(redStatus)
                        }
                    }
                }

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val alertView = inflater.inflate(R.layout.cylinder_info_list_content, parent, false)

            return ViewHolder(alertView)
        }

        override fun getItemCount(): Int {
            return cylinders.count()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val cylinder = cylinders[position]

            holder.cylinderName.text = cylinder.name

            holder.cylinderAddress.text = cylinder.address

            holder.cylinderBasicData.connected = cylinder.connected
            holder.cylinderBasicData.percentage = cylinder.setPosition
            holder.cylinderBasicData.position = cylinder.currPosition
            holder.cylinderBasicData.isManual = cylinder.manualControl
            holder.cylinderBasicData.isCalibrate = cylinder.calibrationMode

//            holder.cylinderBasicData.text = buildString {
//                append(cylinder.currPosition.toString())
//                append("/")
//                append(cylinder.setPosition.toString())
//            }

            //holder.cylinderWarning.isVisible = (cylinder.alertCount > 0)

//            if(cylinder.alertCount > 0) {
//                holder.cylinderWarning.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.resources, R.drawable.alertsicon, null))
//            }
//            else
//            {
//                holder.cylinderWarning.setImageDrawable(null)
//            }


            if(cylinder.connected) {
                if (cylinder.alertCount > 0) {
                    holder.cylinderWarning.setImageDrawable(holder.yellowStatus)
                } else {
                    holder.cylinderWarning.setImageDrawable(holder.greenStatus)
                }
            }
            else
            {
                holder.cylinderWarning.setImageDrawable(holder.redStatus)
            }

            cylinder.setBasicDataListener(holder.cylinderUpdateCallback)

            holder.itemView.setOnClickListener {
                val dashboardData = Bundle().apply {
                    putInt("cylinder_number", position)
                }
                holder.itemView.findNavController().navigate(R.id.dashboardFragment, dashboardData)
            }


        }
    }



    fun saveCylinderList(context: Context)
    {

        val saveList = ArrayList<Cylinder.CylinderData>(cylinderArrayList.size)

        for(cylinder in cylinderArrayList)
        {
            if(cylinder.isSaved) {
                saveList.add(cylinder.getCylinderData())
            }
        }

        val jsonList = Json.encodeToString(saveList)

        val filePath = context.filesDir.path + "/CylinderList.json"

        try{
            Files.createFile(Paths.get(filePath))
        }
        catch(e : Exception)
        {
            Log.d("CylinderList", "Failed to create file: " + e.message + e.toString())
        }

        try{
            File(filePath).writeText(jsonList)
        }
        catch(e : Exception)
        {
            Log.d("CylinderList", "Failed to write to file: " + e.message + e.toString())
        }
    }

    fun loadCylinderList(context: Context)
    {
        cylinderArrayList.clear()
        cylinderListAdapter.notifyDataSetChanged()

        val filePath = context.filesDir.path + "/CylinderList.json"

        try{

            val fileStream = FileInputStream(filePath)
            val cylinderList = Json.decodeFromStream<List<Cylinder.CylinderData>>(fileStream)

            for(someCylinder in cylinderList)
            {
                when(someCylinder.type)
                {
                    "Bluetooth" ->
                    {
                        val newCylinder = BluetoothCylinder(someCylinder.address, someCylinder.name, true)

                        newCylinder.username = someCylinder.username
                        newCylinder.password = someCylinder.password

                        cylinderArrayList.add(newCylinder)

                        newCylinder.connectCylinder(BluetoothConnection, context)

                        cylinderListAdapter.notifyItemInserted(cylinderArrayList.size - 1)
                    }
                }
            }

            fileStream.close()
        }
        catch(e: FileNotFoundException)
        {
            Log.d("CylinderList", "Failed to find file: " + e.message + e.toString())
        }
        catch (e: Exception)
        {
            Log.d("CylinderList", "Failed to read from file: " + e.message + e.toString())
        }

    }

    fun deleteCylinder(position: Int, context: Context)
    {

        getCylinder(position).disconnectCylinder()

        cylinderArrayList.removeAt(position)
        cylinderListAdapter.notifyItemRemoved(position)

        saveCylinderList(context)
    }


    fun disconnectAll()
    {
        for(cylinder in cylinderArrayList)
        {
            cylinder.disconnectCylinder()
        }
    }

    fun reconnectAll()
    {
        for(cylinder in cylinderArrayList)
        {
            if(!cylinder.connected)
            {
                cylinder.connectCylinder()
            }
        }
    }

}