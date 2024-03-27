package com.j2cengineering.motion

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.mikephil.charting.utils.Utils
import com.j2cengineering.motion.cylinders.Cylinder
import com.j2cengineering.motion.cylinders.CylinderList
import com.j2cengineering.motion.databinding.ActivityItemDetailBinding
import com.j2cengineering.motion.databinding.ActivityMotionHostBinding


fun Context.hasPermission(permissionType:String):Boolean{
    return ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasRequiredRuntimePermissions():Boolean{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
    }
    else{
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

private const val RUNTIME_PERMISSION_REQUEST_CODE = 2


var bluetoothDenied = false
private lateinit var appBarConfiguration: AppBarConfiguration

class MotionHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_motion_host)

        val binding = ActivityMotionHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_cylinder_detail) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        Utils.init(baseContext)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_cylinder_detail)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        if(!hasRequiredRuntimePermissions())
        {
            requestRuntimePermissions()
        }
        else
        {
            if (bluetoothHardwareSetup()) {
                //start finding saved cylinders if any
            }
        }


        ApplicationConfiguration.setUpConfiguration(applicationContext)

//        run {
//            CylinderList.loadCylinderList(this.applicationContext)
//        }

    }

    override fun onPause() {
        super.onPause()
        CylinderList.disconnectAll()
    }

    private fun Activity.requestRuntimePermissions()
    {
        if(hasRequiredRuntimePermissions()){return}
        when{
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ->{
                requestLocationPermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->{
                requestBluetoothPermissions()
            }
        }

    }

    private fun requestLocationPermission() {
        runOnUiThread {

            val builder =
                AlertDialog.Builder(this)
            builder.setMessage("Motion requires location permission in order to scan for Bluetooth Low Energy devices and connect to your cylinder. This is not required when the app is not in use.")
            builder.setTitle("Bluetooth Permission required")
            builder.setPositiveButton(
                "OK"
            ) { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
    private fun requestBluetoothPermissions() {
        runOnUiThread {

            val builder =
                AlertDialog.Builder(this)
            builder.setMessage("Motion requires permission in order to scan for Bluetooth Low Energy devices and connect to your cylinder. This is not required when the app is not in use.")
            builder.setTitle("Bluetooth Permission required")
            builder.setPositiveButton(
                "OK"
            ) { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }
            val alertDialog = builder.create()
            alertDialog.show()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            RUNTIME_PERMISSION_REQUEST_CODE -> {
                val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any{
                    it.second == PackageManager.PERMISSION_DENIED && !ActivityCompat.shouldShowRequestPermissionRationale(this, it.first)
                }
                val containsDenial = grantResults.any{it == PackageManager.PERMISSION_DENIED}
                val allGranted = grantResults.all{
                    it == PackageManager.PERMISSION_GRANTED
                }

                when{
                    containsPermanentDenial ->{
                        //something still
                    }

                    containsDenial ->{
                        requestRuntimePermissions()
                    }

                    allGranted && hasRequiredRuntimePermissions() -> {
                        //start trying to reconnect to saved cylinders

                        if (bluetoothHardwareSetup()) {
                            //start finding saved cylinders if any
                        }
                    }

                    else -> {
                        //WTF?
                        recreate()
                    }
                }

            }
        }
    }

    private fun bluetoothHardwareSetup(): Boolean
    {
        BluetoothConnection.setUpBluetoothSystem(this)

        if(BluetoothConnection.checkBleHardware(this) == false)
        {
            val builder =
                AlertDialog.Builder(this)
            builder.setMessage("This device lacks the required Bluetooth Low Energy hardware needed for this app")
            builder.setTitle("Bluetooth Low Energy Missing")

            val alertDialog = builder.create()
            alertDialog.show()

            return false
        }

        if(!BluetoothConnection.isBtEnabled)
        {
            if(!bluetoothDenied) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableRequestResultLauncher.launch(enableBtIntent)
            }
            return false
        }

        return true

    }

    private val enableRequestResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->

        if(result.resultCode == Activity.RESULT_OK) {

            //start finding saved cylinders if any
        }
        else if(result.resultCode == Activity.RESULT_CANCELED)
        {

            val builder =
                AlertDialog.Builder(this)
            builder.setMessage("The Motion app requires the use of Bluetooth to connect you your cylinders, please enable Bluetooth in your network settings.")
            builder.setTitle("Bluetooth Disabled")

            val alertDialog = builder.create()
            alertDialog.show()

            bluetoothDenied = true
        }
    }
}