package com.j2cengineering.motion

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.j2cengineering.motion.cylinders.CylinderList


/**
 * A simple [Fragment] subclass.
 * Use the [CylinderListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CylinderListFragment : Fragment() {

    private lateinit var newConnectionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cylinder_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        newConnectionButton = view.findViewById(R.id.new_connection_button)

        newConnectionButton.setOnClickListener {
            //findNavController().navigate(R.id.cylinderBlueToothConnectFragment)
        }


        val cylinderList = view.findViewById<RecyclerView>(R.id.cylinder_connect_list_recyclerView)
        cylinderList.adapter = CylinderList.getListAdapter()
        cylinderList.layoutManager = LinearLayoutManager(this.context)

        val itemTouchHelper = ItemTouchHelper(swipeController)

        itemTouchHelper.attachToRecyclerView(cylinderList)

        cylinderList.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })

        swipeController.swipeControllerActions = object : SwipeController.SwipeControllerAction()
        {
            override fun onDisconnectClicked(position: Int) {

                if(CylinderList.getCylinder(position).connected) {
                    CylinderList.getCylinder(position).disconnectCylinder()
                }
                else
                {
                    CylinderList.getCylinder(position).connectCylinder()
                }
            }

            override fun onDeleteClicked(position: Int) {
                val deleteDialog = AlertDialog.Builder(context)

                deleteDialog.setMessage("Are you sure you want to delete this connection?\n This will remove all data for this cylinder on this device.")
                deleteDialog.setPositiveButton("Delete", DialogInterface.OnClickListener{
                    dialog, _ ->

                    context?.let { CylinderList.deleteCylinder(position, it) }

                    dialog.dismiss()
                })

                deleteDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })

                deleteDialog.setCancelable(true)

                val delete = deleteDialog.create()

                delete.setTitle("Delete Connection")

                delete.show()
            }
        }

    }

    override fun onResume() {
        super.onResume()

//        CylinderList.setAllBasicDataCallback(object : Cylinder.OnBasicDataListener{
//            override fun onConnectionChange(cylinder: Cylinder) {
//                activity?.runOnUiThread {
//                    CylinderList.getListAdapter()
//                        .notifyItemChanged(CylinderList.indexOf(cylinder))
//                }
//            }
//
//            override fun onSetPositionChange(cylinder: Cylinder) {
//                activity?.runOnUiThread {
//                    CylinderList.getListAdapter().notifyItemChanged(CylinderList.indexOf(cylinder))
//                }
//            }
//
//            override fun onCurrPositionChange(cylinder: Cylinder) {
//                activity?.runOnUiThread {
//                    CylinderList.getListAdapter().notifyItemChanged(CylinderList.indexOf(cylinder))
//                }
//            }
//
//            override fun onStatusChange(cylinder: Cylinder) {
//                activity?.runOnUiThread {
//                    CylinderList.getListAdapter().notifyItemChanged(CylinderList.indexOf(cylinder))
//                }
//            }
//
//            override fun onAlertChange(cylinder: Cylinder) {
//                activity?.runOnUiThread {
//                    CylinderList.getListAdapter().notifyItemChanged(CylinderList.indexOf(cylinder))
//                }
//            }
//
//        })

        CylinderList.startAllBasicData()

    }

    override fun onPause() {
        super.onPause()

        CylinderList.stopAllBasicData()
    }

    private val swipeController = SwipeController()

    class SwipeController :ItemTouchHelper.Callback(){

        enum class ButtonsState{
            GONE,
            RIGHT_VISIBLE
        }

        private var swipeBack = false
        private var buttonShowedState = ButtonsState.GONE
        private val buttonWidth = 300.0f
        private var currentItemViewHolder: RecyclerView.ViewHolder? = null
        private var disconnectButton : RectF? = null
        private var deleteButton: RectF? = null

        var swipeControllerActions: SwipeControllerAction? = null

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {

            return makeMovementFlags(0, ItemTouchHelper.LEFT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {

            if(swipeBack){
                swipeBack = buttonShowedState != ButtonsState.GONE
                return 0
            }

            return super.convertToAbsoluteDirection(flags, layoutDirection)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            var dx = dX

            if (actionState == ACTION_STATE_SWIPE) {
                if (buttonShowedState != ButtonsState.GONE) {
                    if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dx =
                        dx.coerceAtMost(-buttonWidth * 2);
                    super.onChildDraw(c, recyclerView, viewHolder, dx, dY, actionState, isCurrentlyActive);
                }
                else {
                    setTouchListener(c, recyclerView, viewHolder, dx, dY, actionState, isCurrentlyActive);
                }
            }

            if (buttonShowedState == ButtonsState.GONE) {
                super.onChildDraw(c, recyclerView, viewHolder, dx, dY, actionState, isCurrentlyActive);
            }

            currentItemViewHolder = viewHolder
        }

        private fun setTouchListener(c: Canvas, recyclerView : RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
        {
            recyclerView.setOnTouchListener(object: View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    swipeBack = event?.action== MotionEvent.ACTION_CANCEL || event?.action == MotionEvent.ACTION_UP
                    if (swipeBack) {
                        if (dX < -buttonWidth * 2)
                        {
                            buttonShowedState = ButtonsState.RIGHT_VISIBLE
                        }

                        if (buttonShowedState != ButtonsState.GONE) {
                            setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            setItemsClickable(recyclerView, false)
                        }
                    }
                    return false
                }
            })
        }

        private fun setTouchDownListener(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float,
            actionState: Int, isCurrentlyActive: Boolean
        ) {
            recyclerView.setOnTouchListener(object : View.OnTouchListener{
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_DOWN) {
                        setTouchUpListener(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                    return false
                }
            })


        }

        private fun setTouchUpListener(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float,
            actionState: Int, isCurrentlyActive: Boolean
        ) {
            recyclerView.setOnTouchListener(object : View.OnTouchListener{
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_UP) {
                        super@SwipeController.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            0f,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                        recyclerView.setOnTouchListener { v, event -> false }
                        setItemsClickable(recyclerView, true)
                        swipeBack = false


                        if(swipeControllerActions != null)
                        {
                            if(deleteButton?.contains(event.x, event.y) == true)
                            {
                                swipeControllerActions!!.onDeleteClicked(viewHolder.absoluteAdapterPosition)
                            }

                            if(disconnectButton?.contains(event.x, event.y) == true)
                            {
                                swipeControllerActions!!.onDisconnectClicked(viewHolder.absoluteAdapterPosition)
                            }
                        }


                        buttonShowedState = ButtonsState.GONE
                        currentItemViewHolder = null
                    }
                    return false
                }
            })
        }

        private fun setItemsClickable(
            recyclerView: RecyclerView,
            isClickable: Boolean
        ) {
            for (i in 0 until recyclerView.childCount) {
                recyclerView.getChildAt(i).isClickable = isClickable
            }
        }

        private fun drawButtons(c: Canvas, viewHolder: RecyclerView.ViewHolder) {
            val buttonWidthWithoutPadding = buttonWidth
            val corners = 16f
            val itemView = viewHolder.itemView
            val strokePaint = Paint()
            strokePaint.style = Paint.Style.STROKE
            strokePaint.color = Color.BLACK
            strokePaint.strokeWidth = 2f

            val p = Paint()
            p.style = Paint.Style.FILL

            disconnectButton = RectF(
                itemView.right - buttonWidthWithoutPadding * 2,
                itemView.top.toFloat(),
                itemView.right.toFloat() - buttonWidthWithoutPadding,
                itemView.bottom.toFloat()
            )
            p.color = Color.rgb(255, 165, 0)
            //c.drawRoundRect(leftButton, corners, corners, p)
            c.drawRect(disconnectButton!!, p)
            c.drawRect(disconnectButton!!, strokePaint)

            val pos = viewHolder.absoluteAdapterPosition
            val cylinder = CylinderList.getCylinder(pos)

            if(cylinder.connected)
            {
                drawText("DISCONNECT", c, disconnectButton!!, p)
            }
            else
            {
                drawText("CONNECT", c, disconnectButton!!, p)
            }

            deleteButton = RectF(
                itemView.right - buttonWidthWithoutPadding,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            p.color = Color.RED
            //c.drawRoundRect(rightButton, corners, corners, p)
            c.drawRect(deleteButton!!, p)
            c.drawRect(deleteButton!!, strokePaint)
            drawText("DELETE", c, deleteButton!!, p)
        }

        private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
            var textSize = 60f
            p.color = Color.WHITE
            p.isAntiAlias = true
            p.textSize = textSize
            var textWidth: Float = p.measureText(text)

            while(textWidth > buttonWidth - 20)
            {
                textSize -= 0.5f
                p.textSize = textSize
                textWidth = p.measureText(text)
            }

            c.drawText(text, button.centerX() - textWidth / 2, button.centerY() + textSize / 2, p)
        }

        fun onDraw(c: Canvas) {
            if (currentItemViewHolder != null) {
                drawButtons(c, currentItemViewHolder!!)
            }
        }

        abstract class SwipeControllerAction{
            open fun onDeleteClicked(position: Int) {}

            open fun onDisconnectClicked(position: Int) {}
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CylinderListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            CylinderListFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}