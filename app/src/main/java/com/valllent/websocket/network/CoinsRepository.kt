package com.valllent.websocket.network

import android.util.Log
import com.valllent.websocket.data.Coin
import com.valllent.websocket.network.responses.AllCoinsResponse
import com.valllent.websocket.network.responses.PricesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json

class CoinsRepository(
    private val httpClient: HttpClient
) {

    private val jsonDecoder = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    suspend fun getAllCoins(): List<Coin>? {
        val result = httpClient.get {
            url(
                scheme = "https",
                host = "api.coincap.io",
                path = "/v2/assets",
            )
            parameter("ids", "bitcoin,ethereum,monero,litecoin")
        }
        val response = result.body<AllCoinsResponse>()
        return response.convertToCoinList()
    }

    suspend fun subscribeForUpdates(onUpdate: (List<Coin>) -> Unit) {
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
                while (true) {
                    // TODO: Handle another Frame types
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        val body = frame.readText()
                        Log.d("TESTV", "$body")
                        val response = jsonDecoder.decodeFromString<PricesResponse>(body)
                        val updatedPrices = response.convertToCoinList()
                        onUpdate(updatedPrices)
                    } else {
                        Log.d("TESTV", "Unknown prices")
                    }
                }
            }
        } catch (throwable: Throwable) {
            Log.e("TESTV", "${throwable.message}: ${Log.getStackTraceString(throwable)}")
        }
    }

}