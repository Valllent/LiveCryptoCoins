package com.valllent.websocket.utils

import com.valllent.websocket.data.LineGraphData
import java.util.concurrent.ConcurrentLinkedDeque

class LineGraphManager {

    companion object {
        const val LIMIT = 10
    }

    private var maxPrice = 0f
    private var minPrice = 0f
    private val allPrices = ConcurrentLinkedDeque<Float>()

    fun savePrice(price: Float) {
        if (maxPrice == 0f) maxPrice = price
        if (minPrice == 0f) minPrice = price

        if (price > maxPrice) maxPrice = price
        if (price < minPrice) minPrice = price

        if (allPrices.size > LIMIT) {
            allPrices.removeFirst()
        }
        allPrices.add(price)
    }

    fun calculateGraphData(): LineGraphData {
        val prices = allPrices.toTypedArray()
        val dots = ArrayList<Float>(prices.size)
        for (price in prices) {
            val delta = maxPrice - minPrice
            val normalized = if (delta != 0f) {
                (maxPrice - price) / (delta)
            } else {
                0f
            }
            dots.add(normalized)
        }
        return LineGraphData(
            min = minPrice,
            max = maxPrice,
            dots = dots,
        )
    }

}