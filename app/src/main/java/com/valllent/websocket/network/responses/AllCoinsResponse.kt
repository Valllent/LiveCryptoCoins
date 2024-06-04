package com.valllent.websocket.network.responses

import com.valllent.websocket.data.Coin
import kotlinx.serialization.Serializable

@Serializable
data class AllCoinsResponse(
    val data: List<CoinItem>? = null
) {

    @Serializable
    data class CoinItem(
        val id: String? = null,
        val priceUsd: String? = null,
    )

    fun convertToCoinList(): List<Coin>? {
        val result = mutableListOf<Coin>()
        val responseArray = data ?: emptyList()

        for (coin in responseArray) {
            val price = coin.priceUsd?.toFloatOrNull() ?: 0.0f
            result.add(Coin(coin.id ?: continue, price))
        }

        if (result.isEmpty()) return null

        return result
    }

}