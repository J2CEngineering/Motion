package com.j2cengineering.motion.cylinders

import android.content.Context
import com.j2cengineering.motion.BluetoothConnection

object ConnectedDevice {

    private var connectedCylinder: Cylinder? = null

    fun getCylinder(): Cylinder?
    {
        return connectedCylinder
    }

    fun isConnected(): Boolean
    {
        return (connectedCylinder != null && connectedCylinder!!.connected)
    }


    fun connectBluetoothCylinder(address: String, context: Context): Boolean
    {
        val newCylinder = BluetoothCylinder(address)
        newCylinder.connectCylinder(BluetoothConnection, context)


        connectedCylinder = newCylinder


        return connectedCylinder != null
    }

    fun disconnectCylinder()
    {
        connectedCylinder?.disconnectCylinder()

        connectedCylinder = null
    }




}