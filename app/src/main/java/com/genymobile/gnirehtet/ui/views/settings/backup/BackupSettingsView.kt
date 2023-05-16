package com.genymobile.gnirehtet.ui.views.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.domain.Gnirehtet
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.genymobile.gnirehtet.ui.views.settings.SettingItem
import com.genymobile.gnirehtet.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val JSON_MIME_TYPE = "application/json"

@Composable
fun BackupSettingsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    BaseSettingsView(title = "Appearance", navController = navController) {
        LazyColumn {
            item {
                ImportSettings()
            }
            item {
                ExportSettings()
            }
            item {
                ResetSettings()
            }
        }
    }
}

@Composable
private fun ImportSettings() {
    val context = LocalContext.current
    val restartGnirehtetCoroutineScope = rememberCoroutineScope()

    val receiver = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
        result?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { bufferedReader ->
                    val json = bufferedReader.readText()

                    Gnirehtet.launchRestartGnirehtetScope(restartGnirehtetCoroutineScope) {
                        val successful = Preferences.import(context, json)

                        withContext(Dispatchers.Main) {
                            if (successful) {
                                context.toast("Successfully imported settings")
                            } else {
                                context.toast("Failed to import settings")
                            }
                        }
                    }
                }
            }
        }
    }

    SettingItem(
        title = "Import settings",
        description = "Import blocked apps, DNS servers, theme, etc.",
        icon = Icons.Filled.FileOpen
    ) {
        receiver.launch(JSON_MIME_TYPE)
    }
}

@Composable
private fun ExportSettings() {
    val context = LocalContext.current

    val receiver = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument(JSON_MIME_TYPE)) { result ->
        result?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { bufferedWriter ->
                    bufferedWriter.write(Preferences.export())
                    bufferedWriter.flush()
                }
            }
        }
    }

    SettingItem(
        title = "Export settings",
        description = "Export blocked apps, DNS servers, theme, etc.",
        icon = Icons.Filled.UploadFile
    ) {
        receiver.launch("gnirehtet_settings_${System.currentTimeMillis()}.json")
    }
}

@Composable
private fun ResetSettings() {
    val context = LocalContext.current
    val restartGnirehtetCoroutineScope = rememberCoroutineScope()

    SettingItem(
        title = "Reset settings",
        description = "Reset blocked apps, DNS servers, theme, etc.",
        icon = Icons.Filled.RestartAlt
    ) {

        Gnirehtet.launchRestartGnirehtetScope(restartGnirehtetCoroutineScope) {
            Preferences.reset(context)

            withContext(Dispatchers.Main) {
                context.toast("Settings have been reset")
            }
        }
    }
}
