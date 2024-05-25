package com.valllent.websocket.network

import android.util.Log
import com.valllent.websocket.data.Coin
import com.valllent.websocket.network.responses.AllCoinsResponse
import com.valllent.websocket.network.responses.PricesResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class CoinsRepository(
    private val httpClient: HttpClient
) {

    companion object {
        private const val TAG = "CoinsRepository"
    }

    private val jsonDecoder = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    suspend fun getAllCoins(): List<Coin>? {
        val result = runCatching {
            val response = httpClient.get {
                url(
                    scheme = "https",
                    host = "api.coincap.io",
                    path = "/v2/assets",
                )
                parameter("ids", "bitcoin,ethereum,monero,litecoin")
            }
            response.body<AllCoinsResponse>().convertToCoinList()
        }

        return result.getOrNull()
    }

    suspend fun getCoinUpdatesFlow(): Flow<List<Coin>> {
        return flow {
            try {
                httpClient.webSocket(
                    request = {
                        url(
                            scheme = "wss",
                            host = "ws.coincap.io",
                            path = "/prices",
                        )
                        parameter("assets", "bitcoin,ethereum,monero,litecoin")
                    }
                ) {
                    // TODO: Handle another Frame types
                    while (true) {
                        val frame = incoming.receive()
                        if (frame is Frame.Text) {
                            val body = frame.readText()
                            val response = jsonDecoder.decodeFromString<PricesResponse>(body)
                            val updatedPrices = response.convertToCoinList()
                            emit(updatedPrices)
                        } else {
                            Log.e(TAG, "Unknown type: ${frame.javaClass}")
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Log.e(TAG, "Prices flow stopped: ${throwable.message}: ${Log.getStackTraceString(throwable)}")
            }
        }
    }

}