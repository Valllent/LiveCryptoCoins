package com.valllent.websocket.network.responses

import com.valllent.websocket.data.Coin
import kotlinx.serialization.Serializable

@Serializable
data class PricesResponse(
    val bitcoin: String? = null,
    val ethereum: String? = null,
    val monero: String? = null,
    val litecoin: String? = null,
) {

    fun convertToCoinList(): MutableList<Coin> {
        val result = mutableListOf<Coin>()

        addCoinToList(bitcoin, "bitcoin", result)
        addCoinToList(ethereum, "ethereum", result)
        addCoinToList(monero, "monero", result)
        addCoinToList(litecoin, "litecoin", result)

        return result
    }

    private fun addCoinToList(coinPrice: String?, coinName: String, list: MutableList<Coin>) {
        if (coinPrice == null) return

        val price = coinPrice.toFloatOrNull()
        if (price != null) {
            list.add(Coin(coinName, price))
        }
    }

}