package com.genymobile.gnirehtet.ui.views.settings.appearance

import android.os.Build
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.ui.theme.Theme
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.genymobile.gnirehtet.ui.views.settings.PreferenceSwitch
import com.genymobile.gnirehtet.ui.views.settings.SettingItem

@Composable
fun AppearanceSettingsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    BaseSettingsView(title = "Appearance", navController = navController) {
        LazyColumn {
            item {
                ThemeSettings()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    DynamicColorSettings()
                }
            }
        }
    }
}

@Composable
private fun ThemeSettings() {
    val theme by Preferences.theme.stateFlow.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    SettingItem(
        title = "Theme",
        description = theme.displayName,
        icon = Icons.Filled.DarkMode
    ) {
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = { Text("Theme") },
            text = {
                LazyColumn {
                    items(items = Theme.values()) {
                        SettingItem(title = it.displayName) {
                            Preferences.theme.value = it
                            showDialog = false
                        }
                    }
                }
            },
            confirmButton = {},
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
private fun DynamicColorSettings() {
    val dynamicColor by Preferences.dynamicColor.stateFlow.collectAsState()

    PreferenceSwitch(
        title = "Dynamic color",
        description = "Color scheme based off the system wallpaper",
        icon = Icons.Filled.Palette,
        isChecked = dynamicColor,
        onClick = {
            Preferences.dynamicColor.value = it
        }
    )
}
