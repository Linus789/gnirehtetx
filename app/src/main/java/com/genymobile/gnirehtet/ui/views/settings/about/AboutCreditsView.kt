package com.genymobile.gnirehtet.ui.views.settings.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults

@Composable
fun AboutCreditsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    BaseSettingsView(title = "Credits", navController = navController) {
        LibrariesContainer(
            modifier = Modifier.fillMaxSize(),
            colors = LibraryDefaults.libraryColors(
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                badgeBackgroundColor = MaterialTheme.colorScheme.primary,
                badgeContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}
