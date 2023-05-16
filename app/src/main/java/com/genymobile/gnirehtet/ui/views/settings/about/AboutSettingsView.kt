package com.genymobile.gnirehtet.ui.views.settings.about

import android.os.Build
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.BuildConfig
import com.genymobile.gnirehtet.ui.views.Views
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.genymobile.gnirehtet.ui.views.settings.SettingItem
import com.genymobile.gnirehtet.ui.views.utils.navigate
import com.genymobile.gnirehtet.utils.toast

@Composable
fun AboutSettingsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    BaseSettingsView(title = "About", navController = navController) {
        LazyColumn {
            item {
                SettingItem(
                    title = "Version",
                    description = BuildConfig.VERSION_NAME,
                    icon = Icons.Filled.Info,
                ) {
                    clipboardManager.setText(AnnotatedString(getVersionReport()))
                    context.toast("Info copied to clipboard")
                }
            }
            item {
                SettingItem(
                    title = "Changelog",
                    description = "Log of all notable changes for each version",
                    icon = Icons.Filled.Article,
                ) {
                    navController.navigate(Views.SettingsAboutChangelog)
                }
            }
            item {
                SettingItem(
                    title = "License",
                    description = "Show license of this app",
                    icon = Icons.Filled.HistoryEdu,
                ) {
                    navController.navigate(Views.SettingsAboutLicense)
                }
            }
            item {
                SettingItem(
                    title = "Credits",
                    description = "Credits and libre software",
                    icon = Icons.Filled.AutoAwesome,
                ) {
                    navController.navigate(Views.SettingsAboutCredits)
                }
            }
            item {
                SettingItem(
                    title = "Source code",
                    description = "Opens the GitHub repository for this app",
                    icon = Icons.Filled.Code,
                ) {
                    uriHandler.openUri("https://github.com/Linus789/gnirehtetx")
                }
            }
        }
    }
}

private fun getVersionReport(): String {
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    val release = if (Build.VERSION.SDK_INT >= 30) {
        Build.VERSION.RELEASE_OR_CODENAME
    } else {
        Build.VERSION.RELEASE
    }

    return StringBuilder().append("App version: $versionName ($versionCode)\n")
        .append("Device information: Android $release (API ${Build.VERSION.SDK_INT})\n")
        .append("Supported ABIs: ${Build.SUPPORTED_ABIS.contentToString()}\n")
        .toString()
}
