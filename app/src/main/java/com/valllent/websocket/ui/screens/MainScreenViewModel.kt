package com.valllent.websocket.ui.screens

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valllent.websocket.data.Coin
import com.valllent.websocket.network.CoinsRepository
import com.valllent.websocket.utils.LineGraphManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.ref.WeakReference

class MainScreenViewModel(
    private val coinsRepository: CoinsRepository,
    private val connectivityManager: ConnectivityManager,
) : ViewModel() {

    private val _state = createDefaultState()
    val state = _state.asStateFlow()

    private val lineGraphManager = LineGraphManager()
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
                updatesState = UpdatesState.Disconnected,
                lineGraphData = null
            )
        )
    }


    private val allCoins = mutableListOf<Coin>()

    private fun loadAllCoins() {
        viewModelScope.launch {
            _state.update { it.copy(coinsState = CoinsState.Loading) }

            val coins = coinsRepository.getAllCoins()
            if (coins == null) {
                _state.update { it.copy(coinsState = CoinsState.LoadingFailed) }
                return@launch
            }

            allCoins.addAll(coins)
            _state.update { it.copy(coinsState = CoinsState.Downloaded(allCoins)) }
            createUpdatesConnection()
        }
    }

    private fun createUpdatesConnection() {
        startLineGraphCalculations()

        viewModelScope.launch(Dispatchers.IO) {
            val coinsFlow = coinsRepository.getCoinUpdatesFlow()

            _state.update { it.copy(updatesState = UpdatesState.Connected) }
            coinsFlow.collect { newPrices ->
                val coins = updatePrices(newPrices)
                _state.update { it.copy(coinsState = CoinsState.Downloaded(coins)) }
            }
            graphLineCalculationJob?.cancel()
            _state.update { it.copy(updatesState = UpdatesState.Disconnected) }

            reconnectWhenInternetReturn()
        }
    }

    private fun updatePrices(newCoinPrices: List<Coin>): List<Coin> {
        for (coin in newCoinPrices) {
            val index = allCoins.indexOfFirst { it.name == coin.name }
            allCoins[index] = coin
            if (coin.name == "bitcoin") {
                lineGraphManager.savePrice(coin.price)
            }
        }

        return allCoins
    }

    private var graphLineCalculationJob: Job? = null

    private fun startLineGraphCalculations() {
        graphLineCalculationJob?.cancel()
        graphLineCalculationJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                _state.update {
                    it.copy(
                        lineGraphData = lineGraphManager.calculateGraphData()
                    )
                }
                delay(1_000)
            }
        }
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
