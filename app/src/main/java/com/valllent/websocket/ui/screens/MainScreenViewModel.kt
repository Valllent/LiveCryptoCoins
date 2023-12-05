package com.valllent.websocket.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valllent.websocket.data.Coin
import com.valllent.websocket.network.CoinsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val coinsRepository: CoinsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MainScreenState>(MainScreenState.Loading)
    val state = _state.asStateFlow()

    init {
        loadAllCoins()
    }

    fun retryFirstLoading() {
        loadAllCoins()
    }

    private fun loadAllCoins() {
        viewModelScope.launch {
            _state.value = MainScreenState.Loading

            val coins = coinsRepository.getAllCoins()

            if (coins == null) {
                _state.value = MainScreenState.LoadingFailed
                return@launch
            }

            _state.value = MainScreenState.DisplayingData(coins)
            createConnection()
        }
    }

    private fun createConnection() {
        viewModelScope.launch {
            coinsRepository.subscribeForUpdates { newPrices ->
                _state.value = MainScreenState.DisplayingData(modify(newPrices))
            }
        }
    }

    private fun modify(newPrices: List<Coin>): List<Coin> {
        val oldPrices = (state.value as MainScreenState.DisplayingData).coins.toMutableList()

        for (newPrice in newPrices) {
            val oldPriceIndex = oldPrices.indexOfFirst { oldPrice ->
                newPrice.name == oldPrice.name
            }
            oldPrices[oldPriceIndex] = newPrice
        }

        return oldPrices
    }

}