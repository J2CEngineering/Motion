package com.j2cengineering.motion

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AxisTimeValueFormatter(private val chartData:ChartDataSetManager ): ValueFormatter() {

    private val formatter = DateTimeFormatter.ofPattern("mm:ss")

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val labelTime = if(chartData.liveData) {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(((value * 1000.0f)).toLong() + chartData.creationEpoch),
                TimeZone.getDefault().toZoneId()
            )
        }else
        {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(((value * 1000.0f)).toLong() + chartData.loadedDataEpoch),
                TimeZone.getDefault().toZoneId())
        }
        return labelTime.format(formatter)

    }

}