package com.valllent.websocket.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.valllent.websocket.ui.screens.MainScreenActions
import com.valllent.websocket.ui.screens.MainScreenView
import com.valllent.websocket.ui.screens.MainScreenViewModel
import com.valllent.websocket.ui.theme.WebSocketTheme
import org.koin.androidx.compose.getViewModel


@Composable
fun Wrapper() {
    WebSocketTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val viewModel = getViewModel<MainScreenViewModel>()
            val state = viewModel.state.collectAsState().value
            val actions = MainScreenActions(
                onRetryFirstLoading = {
                    viewModel.retryFirstLoading()
                }
            )
            MainScreenView(state, actions)
        }
    }
}
