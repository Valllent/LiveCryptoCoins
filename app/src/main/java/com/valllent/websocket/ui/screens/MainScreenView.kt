package com.valllent.websocket.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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


sealed class MainScreenState {

    object Loading : MainScreenState()

    object LoadingFailed : MainScreenState()

    data class DisplayingData(
        val coins: List<Coin>
    ) : MainScreenState()

}

data class MainScreenActions(
    val onRetryFirstLoading: () -> Unit,
)

@Composable
fun MainScreenView(
    state: MainScreenState,
    actions: MainScreenActions,
) {
    when (state) {
        is MainScreenState.DisplayingData -> {
            val coins = state.coins
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                HeaderWithLivePrices(coins)
            }
        }

        is MainScreenState.Loading -> {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MainScreenState.LoadingFailed -> {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {}
                ) {
                    Text(
                        text = stringResource(id = R.string.retry)
                    )
                }
            }
        }
    }
}


@Composable
private fun HeaderWithLivePrices(coins: List<Coin>) {
    ConstraintLayout(
        modifier = Modifier
            .padding(4.dp)
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
                            .weight(1 / 2f)
                            .padding(end = 8.dp),
                        text = coin.name.uppercase(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        fontSize = 24.sp,
                    )
                    Text(
                        modifier = Modifier
                            .weight(1 / 2f)
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

                width = Dimension.value(2.dp)
                height = Dimension.fillToConstraints
            },
            color = Color.Red
        )
    }
}


private val mockActions = MainScreenActions {}

@Preview(showBackground = true)
@Composable
private fun MainScreen_Loading() {
    MainScreenView(MainScreenState.Loading, mockActions)
}