package com.valllent.websocket.ui.screens

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valllent.websocket.data.Coin
import com.valllent.websocket.network.CoinsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainScreenViewModel(
    private val coinsRepository: CoinsRepository,
    private val connectivityManager: ConnectivityManager,
) : ViewModel() {

    private val _state = createDefaultState()
    val state = _state.asStateFlow()

    private var networkReconnectionCallback: ConnectivityManager.NetworkCallback? = null

    init {
        loadAllCoins()
    }


    fun retryFirstLoading() {
        loadAllCoins()
    }

    fun recreateUpdatesConnection() {
        createUpdatesConnection()
    }

    override fun onCleared() {
        super.onCleared()
        networkReconnectionCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }


    private fun createDefaultState(): MutableStateFlow<MainScreenState> {
        return MutableStateFlow(
            MainScreenState(
                coinsState = CoinsState.Loading,
                updatesState = UpdatesState.Disconnected
            )
        )
    }

    private fun loadAllCoins() {
        viewModelScope.launch {
            _state.update { it.copy(coinsState = CoinsState.Loading) }

            val coins = coinsRepository.getAllCoins()
            if (coins == null) {
                _state.update { it.copy(coinsState = CoinsState.LoadingFailed) }
                return@launch
            }

            _state.update { it.copy(coinsState = CoinsState.Downloaded(coins)) }
            createUpdatesConnection()
        }
    }

    private fun createUpdatesConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            val coinsFlow = coinsRepository.getCoinUpdatesFlow()

            _state.update { it.copy(updatesState = UpdatesState.Connected) }
            coinsFlow.collect { newPrices ->
                _state.update { it.copy(coinsState = CoinsState.Downloaded(updatePrices(newPrices))) }
            }
            _state.update { it.copy(updatesState = UpdatesState.Disconnected) }

            reconnectWhenInternetReturn()
        }
    }

    private fun updatePrices(newPrices: List<Coin>): List<Coin> {
        val state = state.value.coinsState as? CoinsState.Downloaded ?: return emptyList()
        val oldPrices = state.coins.toMutableList()

        for (newPrice in newPrices) {
            val oldPriceIndex = oldPrices.indexOfFirst { oldPrice ->
                newPrice.name == oldPrice.name
            }
            oldPrices[oldPriceIndex] = newPrice
        }

        return oldPrices
    }

    private fun reconnectWhenInternetReturn() {
        if (networkReconnectionCallback != null) return

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val createNetworkListener = createNetworkListener(this).also {
            networkReconnectionCallback = it
        }
        connectivityManager.registerNetworkCallback(
            networkRequest,
            createNetworkListener
        )
    }

    companion object {
        /**
         * Memory leak protection.
         */
        private fun createNetworkListener(viewModel: MainScreenViewModel) =
            object : ConnectivityManager.NetworkCallback() {

                private val weakViewModel = WeakReference(viewModel)

                override fun onAvailable(network: Network) {
                    weakViewModel.get()?.recreateUpdatesConnection()
                }
            }
    }

}
