package com.valllent.websocket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.valllent.websocket.R
import com.valllent.websocket.data.Coin


data class MainScreenState(
    val coinsState: CoinsState,
    val updatesState: UpdatesState,
)

sealed class UpdatesState {
    object Connected : UpdatesState()

    object Disconnected : UpdatesState()
}

sealed class CoinsState {

    object Loading : CoinsState()

    object LoadingFailed : CoinsState()

    data class Downloaded(
        val coins: List<Coin>,
    ) : CoinsState()

}

data class MainScreenActions(
    val onRetryFirstLoading: () -> Unit,
    val onRecreateUpdatesConnection: () -> Unit,
)

@Composable
fun MainScreenView(
    state: MainScreenState,
    actions: MainScreenActions,
) {
    LoadingWrapper(state, actions) { coinsState ->
        val coins = coinsState.coins
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            HeaderWithLivePrices(state, actions)

            Divider(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(1.dp),
                color = Color.Red,
            )
        }
    }
}

@Composable
private fun HeaderWithLivePrices(state: MainScreenState, actions: MainScreenActions) {
    val downloadedState = state.coinsState as? CoinsState.Downloaded ?: return
    val coins = downloadedState.coins

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(top = 10.dp, bottom = 14.dp)
                .fillMaxWidth(),
        ) {
            val (coinsGroup, divider) = createRefs()

            Column(
                modifier = Modifier.constrainAs(coinsGroup) {
                    top.linkTo(parent.top)
                    centerHorizontallyTo(parent)

                    width = Dimension.matchParent
                },
            ) {
                for (coin in coins) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            text = coin.name.uppercase(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            fontSize = 24.sp,
                        )
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            text = String.format("%.2f", coin.price),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 24.sp,
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.constrainAs(divider) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    centerHorizontallyTo(parent)

                    width = Dimension.value(4.dp)
                    height = Dimension.fillToConstraints
                },
                color = Color.Red
            )
        }

        if (state.updatesState == UpdatesState.Disconnected) {
            Column(
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.updates_connection_lost),
                    textAlign = TextAlign.Center,
                    color = Color.Red,
                    fontSize = 12.sp,
                )
                RetryButton(
                    onClick = actions.onRecreateUpdatesConnection
                )
            }
        }
    }
}


@Composable
private fun LoadingWrapper(
    state: MainScreenState,
    actions: MainScreenActions,
    content: @Composable (CoinsState.Downloaded) -> Unit,
) {
    when (state.coinsState) {
        is CoinsState.Downloaded -> {
            content(state.coinsState)
        }

        is CoinsState.Loading -> {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is CoinsState.LoadingFailed -> {
            RetryButton(
                onClick = actions.onRetryFirstLoading,
            )
        }
    }
}

@Composable
private fun RetryButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick
        ) {
            Text(
                text = stringResource(id = R.string.retry)
            )
        }
    }
}


private val mockActions = MainScreenActions({}, {})

@Preview(showBackground = true)
@Composable
private fun MainScreen_Loading() {
//    MainScreenView(CoinsState.Loading, mockActions)
}