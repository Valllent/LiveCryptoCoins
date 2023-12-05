package com.valllent.websocket.di

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
                MainScreenViewModel(get())
            }

            single {
                CoinsRepository(get())
            }
            single {
                ConfiguredHttpClient.create()
            }
        }
    }

}