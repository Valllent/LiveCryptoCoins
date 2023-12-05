package com.valllent.websocket.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ConfiguredHttpClient {

    fun create(): HttpClient {
        return HttpClient(OkHttp) {
            install(WebSockets)

            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            engine {
                preconfigured = OkHttpClient.Builder()
                    .pingInterval(5, TimeUnit.SECONDS)
                    .build()
            }
        }
    }

}