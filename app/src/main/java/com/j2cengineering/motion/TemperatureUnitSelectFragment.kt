package com.j2cengineering.motion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/**
 * A simple [Fragment] subclass.
 * Use the [TemperatureUnitSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TemperatureUnitSelectFragment : DialogFragment() {

    private lateinit var fahrenheitRadioButton: RadioButton
    private lateinit var celsiusRadioButton: RadioButton
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

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
        return inflater.inflate(R.layout.fragment_temperature_unit_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fahrenheitRadioButton = view.findViewById(R.id.fahrenheitRadioButton)
        celsiusRadioButton = view.findViewById(R.id.celsiusRadioButton)
        cancelButton = view.findViewById(R.id.temperatureCancelButton)
        saveButton = view.findViewById(R.id.temperatureSaveButton)

        cancelButton.setOnClickListener {
            this.dismiss()
        }

        saveButton.setOnClickListener {

            ApplicationConfiguration.TemperatureUnitIsFahrenheit = fahrenheitRadioButton.isChecked
            this.dismiss()

        }
    }

    override fun onResume() {
        super.onResume()

        fahrenheitRadioButton.isChecked = ApplicationConfiguration.TemperatureUnitIsFahrenheit == true
        celsiusRadioButton.isChecked = ApplicationConfiguration.TemperatureUnitIsFahrenheit != true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TemperatureUnitSelectFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            TemperatureUnitSelectFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}