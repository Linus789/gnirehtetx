package com.genymobile.gnirehtet.ui.views.settings.general

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.domain.Gnirehtet
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.ui.views.Views
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.genymobile.gnirehtet.ui.views.settings.PreferenceSwitch
import com.genymobile.gnirehtet.ui.views.settings.SettingItem
import com.genymobile.gnirehtet.ui.views.utils.navigate

private const val DEFAULT_DNS = "8.8.8.8"

// https://www.regexpal.com/?fam=104038
private val IPv4_IPv6_REGEX = Regex("((^((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\\s*\$)|(^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\$))")

@Composable
fun GeneralSettingsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    BaseSettingsView(title = "General", navController = navController) {
        LazyColumn {
            item {
                DnsServersSettings()
            }
            item {
                StopOnDisconnectSettings()
            }
            item {
                ShowToastOnConnectSettings()
            }
            item {
                ShowToastOnDisconnectSettings()
            }
            item {
                SettingItem(
                    title = "Overwrite settings",
                    description = "Overwrite server-side settings with app ones",
                    icon = Icons.Filled.FrontHand
                ) {
                    navController.navigate(Views.SettingsGeneralOverwrite)
                }
            }
        }
    }
}

@Composable
private fun DnsServersSettings() {
    val context = LocalContext.current
    val dnsServers by Preferences.getGnirehtetDnsServers().collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val restartGnirehtetCoroutineScope = rememberCoroutineScope()

    SettingItem(
        title = "Custom DNS servers",
        description = dnsServers.ifEmpty { DEFAULT_DNS }.replace(" ", ""),
        icon = Icons.Filled.Dns
    ) {
        showDialog = true
    }

    if (showDialog) {
        var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text = dnsServers)) }
        val isError by remember {
            derivedStateOf {
                textFieldValue.text.let {
                    if (it.isEmpty()) {
                        return@let false
                    }

                    it.split(',').any { address ->
                        val formattedAddress = address.trim(' ')
                        !IPv4_IPv6_REGEX.matches(formattedAddress)
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = { Text("Custom DNS servers") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val focusRequester = remember { FocusRequester() }

                    Text("Enter your preferred DNS servers separated by a comma")
                    Spacer(modifier = Modifier.height(20.dp))
                    TextField(
                        value = textFieldValue,
                        singleLine = true,
                        isError = isError,
                        placeholder = {
                            Text(DEFAULT_DNS)
                        },
                        trailingIcon = {
                            if (isError) {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = "Invalid input",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        onValueChange = {
                            textFieldValue = it
                        },
                        modifier = Modifier.focusRequester(focusRequester),
                    )

                    SideEffect {
                        focusRequester.requestFocus()
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isError,
                    onClick = {
                        showDialog = false

                        Gnirehtet.launchRestartGnirehtetScope(restartGnirehtetCoroutineScope) {
                            Preferences.setGnirehtetDnsServers(context, textFieldValue.text)
                        }
                    }
                ) {
                    Text("Save".uppercase())
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel".uppercase())
                }
            },
        )
    }
}

@Composable
private fun StopOnDisconnectSettings() {
    val context = LocalContext.current
    val stopOnDisconnect by Preferences.shouldStopOnDisconnect().collectAsStateWithLifecycle()

    PreferenceSwitch(
        title = "Stop on disconnect",
        description = "Automatically stop Gnirehtet on disconnect",
        icon = Icons.Filled.Cancel,
        isChecked = stopOnDisconnect,
        onClick = {
            Preferences.setStopOnDisconnect(context, it)
        }
    )
}

@Composable
private fun ShowToastOnConnectSettings() {
    val showToastOnConnect by Preferences.gnirehtetShowToastOnConnect.stateFlow.collectAsStateWithLifecycle()

    PreferenceSwitch(
        title = "Show toast on connect",
        description = "Shows a small info box when a connection has been established",
        icon = Icons.Filled.TipsAndUpdates,
        isChecked = showToastOnConnect,
        onClick = {
            Preferences.gnirehtetShowToastOnConnect.value = it
        }
    )
}

@Composable
private fun ShowToastOnDisconnectSettings() {
    val showToastOnDisconnect by Preferences.gnirehtetShowToastOnDisconnect.stateFlow.collectAsStateWithLifecycle()

    PreferenceSwitch(
        title = "Show toast on disconnect",
        description = "Shows a small info box when the connection failed",
        icon = Icons.Filled.TipsAndUpdates,
        isChecked = showToastOnDisconnect,
        onClick = {
            Preferences.gnirehtetShowToastOnDisconnect.value = it
        }
    )
}
