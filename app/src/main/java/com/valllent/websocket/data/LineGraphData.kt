package com.valllent.websocket.data

/**
 * @param dots percentages from 0 to 1
 */
data class LineGraphData(
    val min: Float,
    val max: Float,
    val dots: List<Float>,
)