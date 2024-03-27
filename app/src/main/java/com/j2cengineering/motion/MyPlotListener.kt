package com.j2cengineering.motion

import android.graphics.Canvas

public interface MyPlotListener {

    fun OnBeforeDraw(chart: ThreadSafeLineChart, canvas: Canvas?)

    fun OnAfterDraw(chart: ThreadSafeLineChart, canvas: Canvas?)

    fun OnBeforeNotifySetChange(chart: ThreadSafeLineChart)

    fun OnAfterNotifySetChange(chart: ThreadSafeLineChart)

}