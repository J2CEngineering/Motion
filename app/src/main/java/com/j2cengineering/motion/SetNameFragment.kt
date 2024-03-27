package com.j2cengineering.motion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.j2cengineering.motion.cylinders.CylinderList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val SETTINGS_PARAM1 = "cylinder_number"

/**
 * A simple [Fragment] subclass.
 * Use the [SetNameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SetNameFragment : DialogFragment() {
    private var cylinderNumber: Int = -1

    private lateinit var newNameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button


    interface OnInputListener{
        fun sendInput(input:String)
    }
    var mOnInputListener: OnInputListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cylinderNumber = it.getInt(SETTINGS_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newNameEditText = view.findViewById(R.id.cylinderNameEditText)
        saveButton = view.findViewById(R.id.nameSaveButton)
        cancelButton = view.findViewById(R.id.nameCancelButton)

        cancelButton.setOnClickListener {
            this.dismiss()
        }

        saveButton.setOnClickListener {

            if(mOnInputListener != null)
            {
                mOnInputListener?.sendInput(newNameEditText.text.toString())
            }

            this.dismiss()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SetNameFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            SetNameFragment().apply {
                arguments = Bundle().apply {
                    putInt(SETTINGS_PARAM1, param1)
                }
            }
    }
}