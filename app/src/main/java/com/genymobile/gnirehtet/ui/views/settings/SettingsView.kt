package com.genymobile.gnirehtet.ui.views.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.ui.views.Views
import com.genymobile.gnirehtet.ui.views.utils.GoBack
import com.genymobile.gnirehtet.ui.views.utils.navigate

private const val horizontal = 8

@Composable
fun SettingsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    BaseSettingsView(title = "Settings", navController = navController) {
        LazyColumn {
            item {
                SettingItem(
                    title = "General",
                    description = "Custom DNS servers, stop on disconnect",
                    icon = Icons.Filled.SettingsApplications
                ) {
                    navController.navigate(Views.SettingsGeneral)
                }
            }
            item {
                val description = remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        "Dark theme, dynamic color"
                    } else {
                        "Dark theme"
                    }
                }

                SettingItem(
                    title = "Appearance",
                    description = description,
                    icon = Icons.Filled.Palette
                ) {
                    navController.navigate(Views.SettingsAppearance)
                }
            }
            item {
                SettingItem(
                    title = "Backup",
                    description = "For blocked apps, DNS servers, theme, etc.",
                    icon = Icons.Filled.Save
                ) {
                    navController.navigate(Views.SettingsBackup)
                }
            }
            item {
                SettingItem(
                    title = "About",
                    description = "Version, license, credits",
                    icon = Icons.Filled.Info
                ) {
                    navController.navigate(Views.SettingsAbout)
                }
            }
        }
    }
}


@Composable
fun BaseSettingsView(title: String, navController: NavHostController, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 0.dp, end = 0.dp, top = 10.dp, bottom = 20.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
            GoBack(navController = navController, title = title)
        }

        Spacer(modifier = Modifier.height(10.dp))

        content()
    }
}

// From: https://github.com/JunkFood02/Seal/blob/main/app/src/main/java/com/junkfood/seal/ui/component/SettingItem.kt
@Composable
fun PreferenceItemTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun PreferenceItemDescription(description: String) {
    Text(
        text = description,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun SettingItem(title: String, description: String? = null, icon: ImageVector? = null, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = horizontal.dp, end = 16.dp)
                        .size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (icon == null) 12.dp else 0.dp)
            ) {
                PreferenceItemTitle(title)

                description?.let {
                    PreferenceItemDescription(it)
                }
            }
        }
    }
}

@Composable
fun PreferenceSwitch(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    onClick: ((Boolean) -> Unit) = {},
) {
    Surface(
        modifier = Modifier.toggleable(value = isChecked,
            enabled = enabled,
            onValueChange = { onClick(it) })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, 16.dp)
                .padding(start = if (icon == null) 12.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(32.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                PreferenceItemTitle(title)

                if (!description.isNullOrEmpty()) {
                    PreferenceItemDescription(description)
                }
            }
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 20.dp, end = 6.dp),
                enabled = enabled,
            )
        }
    }
}
