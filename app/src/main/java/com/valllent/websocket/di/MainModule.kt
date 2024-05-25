package com.valllent.websocket.di

import android.content.Context
import android.net.ConnectivityManager
import com.valllent.websocket.network.CoinsRepository
import com.valllent.websocket.network.ConfiguredHttpClient
import com.valllent.websocket.ui.screens.MainScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object MainModule {

    operator fun invoke(): Module {
        return module {
            viewModel {
                MainScreenViewModel(get(), get())
            }

            single {
                CoinsRepository(get())
            }
            single {
                ConfiguredHttpClient.create()
            }
            single {
                val context = get<Context>()
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            }
        }
    }

}