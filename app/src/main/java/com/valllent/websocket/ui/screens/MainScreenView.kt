package com.valllent.websocket.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.valllent.websocket.R
import com.valllent.websocket.data.Coin
import com.valllent.websocket.data.LineGraphData
import com.valllent.websocket.utils.LineGraphManager
import kotlin.math.abs
import kotlin.math.min


data class MainScreenState(
    val coinsState: CoinsState,
    val updatesState: UpdatesState,
    val lineGraphData: LineGraphData?,
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
    LoadingWrapper(state, actions) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            HeaderWithLivePrices(state, actions)

            Divider(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(1.dp),
                color = Color.Red,
            )

            if (state.lineGraphData != null) {
                LineChart(state.lineGraphData)
            }
        }
    }
}



@Composable
private fun LineChart(
    lineGraphData: LineGraphData,
    maxPrices: Int = LineGraphManager.LIMIT,
    heightDp: Int = 300,
    dotSizeDp: Int = 4,
) {
    val dots = lineGraphData.dots

    Text(
        modifier = Modifier
            .padding(end = 8.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.bitcoin).uppercase(),
        maxLines = 1,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    )

    Canvas(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(CircleShape.copy(CornerSize(8.dp))),
    ) {
        val padding = 16.dp.toPx()
        drawRect(
            brush = SolidColor(Color.LightGray),
            size = Size(size.width, size.height),
        )
        withTransform({
            inset(horizontal = padding + 10f, vertical = padding)
        }) {
            drawLine(
                brush = SolidColor(Color.Blue),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2.dp.toPx(),
            )
            drawLine(
                brush = SolidColor(Color.Red),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2.dp.toPx(),
            )

            var previousDotHeight: Float? = null
            val dotsCount = min(maxPrices, dots.size)
            for (i in 0 until dotsCount) {
                val paddingBetweenDots = size.width / (dotsCount - 1)
                val dotWidth = paddingBetweenDots * i
                val dotHeight = abs(dots[i] - 1f) * size.height

                if (previousDotHeight != null) {
                    val growing = previousDotHeight > dotHeight
                    drawLine(
                        color = if (growing) Color.Blue else Color.Red,
                        start = Offset(dotWidth - paddingBetweenDots, previousDotHeight),
                        end = Offset(dotWidth, dotHeight),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                    )
                }

                drawCircle(
                    brush = SolidColor(Color.Red),
                    radius = dotSizeDp.dp.toPx(),
                    center = Offset(dotWidth, dotHeight),
                )

                previousDotHeight = dotHeight
            }
        }
    }

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = String.format("%.2f", lineGraphData.max),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.SemiBold,
        color = Color.Blue,
    )
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = String.format("%.2f", lineGraphData.min),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.SemiBold,
        color = Color.Red,
    )
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
    content: @Composable () -> Unit,
) {
    when (state.coinsState) {
        is CoinsState.Downloaded -> {
            content()
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