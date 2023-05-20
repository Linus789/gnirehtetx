package com.genymobile.gnirehtet.ui.views.settings.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.domain.Gnirehtet
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.genymobile.gnirehtet.ui.views.settings.PreferenceSwitch

@Composable
fun OverwriteSettingsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    BaseSettingsView(title = "Overwrite", navController = navController) {
        LazyColumn {
            item {
                OverwriteDnsServersSettings()
            }
            item {
                OverwriteStopOnDisconnectSettings()
            }
            item {
                OverwriteBlockedAppsSettings()
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Divider(modifier = Modifier.fillMaxWidth(0.9f))
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = "Disabling an overwrite does only have an effect on the next connection.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OverwriteDnsServersSettings() {
    val context = LocalContext.current
    val restartGnirehtetCoroutineScope = rememberCoroutineScope()
    val overwriteDnsServers by Preferences.isOverwriteDnsServers().collectAsStateWithLifecycle()

    PreferenceSwitch(
        title = "Overwrite DNS servers",
        description = "Overwrite the server provided DNS servers with the app ones",
        isChecked = overwriteDnsServers,
        onClick = { newValue ->
            Gnirehtet.launchRestartGnirehtetScope(restartGnirehtetCoroutineScope) {
                Preferences.setOverwriteDnsServers(context, newValue)
            }
        }
    )
}

@Composable
private fun OverwriteStopOnDisconnectSettings() {
    val restartGnirehtetCoroutineScope = rememberCoroutineScope()
    val overwriteStopOnDisconnect by Preferences.isOverwriteStopOnDisconnect().collectAsStateWithLifecycle()

    PreferenceSwitch(
        title = "Overwrite stop on disconnect",
        description = "Overwrite the server provided stop on disconnect setting with the app one",
        isChecked = overwriteStopOnDisconnect,
        onClick = { newValue ->
            Gnirehtet.launchRestartGnirehtetScope(restartGnirehtetCoroutineScope) {
                Preferences.setOverwriteStopOnDisconnect(newValue)
            }
        }
    )
}

@Composable
private fun OverwriteBlockedAppsSettings() {
    val context = LocalContext.current
    val restartGnirehtetCoroutineScope = rememberCoroutineScope()
    val overwriteBlockedApps by Preferences.isOverwriteBlockedApps().collectAsStateWithLifecycle()

    PreferenceSwitch(
        title = "Overwrite blocked apps",
        description = "Overwrite the server provided blocked apps with the app ones",
        isChecked = overwriteBlockedApps,
        onClick = { newValue ->
            Gnirehtet.launchRestartGnirehtetScope(restartGnirehtetCoroutineScope) {
                Preferences.setOverwriteBlockedApps(context, newValue)
            }
        }
    )
}
