package com.drdisagree.colorblendr.ui.compose.screens.pairing

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import com.drdisagree.colorblendr.utils.app.AppUtil
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import androidx.appcompat.R as AppCompatR

@Composable
fun PairingScreen(
    onPairDevice: () -> Unit,
    onDeviceConnected: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var hasNotificationPermission by remember {
        mutableStateOf(AppUtil.hasNotificationPermission(context))
    }
    var isWifiConnected by remember {
        mutableStateOf(SystemUtil.isConnectedToWifi(context))
    }

    DisposableEffect(Unit) {
        onPairDevice()

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isWifiConnected = connectivityManager.getNetworkCapabilities(network)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            }

            override fun onLost(network: Network) {
                isWifiConnected = false
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    LifecycleResumeEffect(Unit) {
        hasNotificationPermission = AppUtil.hasNotificationPermission(context)
        if (WifiAdbShell.isMyDeviceConnected()) {
            onDeviceConnected()
        }
        onPauseOrDispose {}
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.pairing),
                showBackButton = true,
                lifted = scrollState.value > 0
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = dimensionResource(R.dimen.container_margin_horizontal),
                        vertical = dimensionResource(R.dimen.container_margin_bottom)
                    )
            ) {
                if (hasNotificationPermission) {
                    IconTextCard(
                        text = stringResource(R.string.pairing_notification_hint),
                        icon = painterResource(R.drawable.ic_notification),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    ErrorActionCard(
                        text = stringResource(R.string.notification_access_not_granted),
                        icon = painterResource(R.drawable.ic_notification_error),
                        buttonText = stringResource(R.string.notification_settings),
                        buttonIcon = painterResource(R.drawable.ic_open_in_new),
                        onButtonClick = { AppUtil.openAppNotificationSettings(context) },
                        modifier = Modifier.padding(top = 15.dp)
                    )
                }

                IconTextCard(
                    text = stringResource(R.string.notification_style_error),
                    icon = painterResource(R.drawable.ic_warning),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(
                        top = dimensionResource(R.dimen.container_margin_bottom)
                    )
                )

                if (!isWifiConnected) {
                    ErrorActionCard(
                        text = stringResource(R.string.wifi_connection_required),
                        icon = painterResource(R.drawable.ic_no_wifi),
                        buttonText = stringResource(R.string.enable_wifi),
                        buttonIcon = painterResource(R.drawable.ic_wifi_settings),
                        onButtonClick = { SystemUtil.requestEnableWifi(context) },
                        modifier = Modifier.padding(top = 15.dp)
                    )
                }

                GuideCard(
                    onDeveloperOptionsClick = { SystemUtil.openDeveloperOptions(context) },
                    modifier = Modifier.padding(
                        top = dimensionResource(R.dimen.container_margin_bottom)
                    )
                )
            }
        }
    }
}

@Composable
private fun IconTextCard(
    text: String,
    icon: Painter,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = text,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ErrorActionCard(
    text: String,
    icon: Painter,
    buttonText: String,
    buttonIcon: Painter,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onErrorContainer,
                        contentColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        painter = buttonIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .width(ButtonDefaults.IconSize)
                            .height(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(text = buttonText)
                }
            }
        }
    }
}

@Composable
private fun GuideCard(
    onDeveloperOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controlNormal = themeAttrColor(AppCompatR.attr.colorControlNormal)

    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row {
                Icon(
                    painter = painterResource(R.drawable.ic_counter_one),
                    contentDescription = null,
                    tint = controlNormal,
                    modifier = Modifier.padding(top = 5.dp, end = 20.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.wireless_debugging_guide_1),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = stringResource(R.string.wireless_debugging_important_notice),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Button(onClick = onDeveloperOptionsClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_new),
                            contentDescription = null,
                            modifier = Modifier
                                .width(ButtonDefaults.IconSize)
                                .height(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(R.string.developer_options))
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_counter_two),
                    contentDescription = null,
                    tint = controlNormal,
                    modifier = Modifier.padding(end = 20.dp)
                )
                Text(text = stringResource(R.string.wireless_debugging_guide_2))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_counter_three),
                    contentDescription = null,
                    tint = controlNormal,
                    modifier = Modifier.padding(end = 20.dp)
                )
                Text(text = stringResource(R.string.wireless_debugging_guide_3))
            }
        }
    }
}

@Preview
@Composable
private fun PairingScreenPreview() {
    ColorBlendrTheme {
        PairingScreen(onPairDevice = {}, onDeviceConnected = {})
    }
}
