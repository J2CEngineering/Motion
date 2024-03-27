package com.j2cengineering.motion

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.utils.Utils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ThreadSafeLineChart : LineChart {

    private val listeners = ArrayList<MyPlotListener?>()


    init{

    }

    constructor(context: Context?): super(context) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle){}

    @Synchronized
    fun addListener(listener: MyPlotListener?): Boolean {
        return !listeners.contains(listener) && listeners.add(listener)
    }

    @Synchronized
    fun removeListener(listener: MyPlotListener?): Boolean {
        return listeners.remove(listener)
    }


    override fun onDraw(canvas: Canvas?) {

        for (listener in listeners) {
            listener?.OnBeforeDraw(this, canvas)
        }

        super.onDraw(canvas)

        for (listener in listeners) {
            listener?.OnAfterDraw(this, canvas)
        }
    }

    override fun notifyDataSetChanged() {

        for(listener in listeners)
        {
            listener?.OnBeforeNotifySetChange(this)
        }
        super.notifyDataSetChanged()

        for(listener in listeners)
        {
            listener?.OnAfterNotifySetChange(this)
        }
    }
}