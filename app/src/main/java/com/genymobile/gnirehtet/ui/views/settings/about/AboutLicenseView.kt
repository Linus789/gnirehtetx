package com.genymobile.gnirehtet.ui.views.settings.about

import android.content.pm.ActivityInfo
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.R
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.genymobile.gnirehtet.utils.getActivity
import com.genymobile.gnirehtet.utils.getRawTextFile

@Composable
fun AboutLicenseView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current

    context.getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    BaseSettingsView(title = "License", navController = navController) {
        SelectionContainer {
            Text(
                text = context.resources.getRawTextFile(R.raw.license),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        }
    }
}
