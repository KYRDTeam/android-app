package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.chart.ChartResponseEntity
import com.kyberswap.android.domain.model.Chart
import javax.inject.Inject

class ChartMapper @Inject constructor() {
    fun transform(chartResponseEntity: ChartResponseEntity): Chart {

        return Chart(chartResponseEntity)
    }
}