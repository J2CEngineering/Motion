package com.j2cengineering.motion

import android.content.Context
import android.content.SharedPreferences

object ApplicationConfiguration
{

    private var sharedPreferences: SharedPreferences? = null
    private var appContext: Context? = null

    fun setUpConfiguration(context: Context)
    {
        appContext = context
        sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        TemperatureUnitIsFahrenheit = sharedPreferences?.getBoolean(appContext?.getString(R.string.saved_temperature_units), true) == true
    }

    public var TemperatureUnitIsFahrenheit = true
        set(value) {
            field = value
            val editor = sharedPreferences?.edit()

            if(editor != null)
            {
                if (value != null) {
                    editor.putBoolean(appContext?.getString(R.string.saved_temperature_units), value)
                }
                editor.apply()
            }
        }

}