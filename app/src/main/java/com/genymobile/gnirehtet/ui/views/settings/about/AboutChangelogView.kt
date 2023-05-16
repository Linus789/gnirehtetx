package com.genymobile.gnirehtet.ui.views.settings.about

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.R
import com.genymobile.gnirehtet.ui.views.settings.BaseSettingsView
import com.genymobile.gnirehtet.utils.getRawTextFile
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import com.halilibo.richtext.ui.RichTextThemeIntegration

@Composable
fun AboutChangelogView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current

    BaseSettingsView(title = "License", navController = navController) {
        SelectionContainer {
            RichTextThemeIntegration(contentColor = { MaterialTheme.colorScheme.onBackground }) {
                RichText(modifier = Modifier.padding(16.dp)) {
                    Markdown(content = context.resources.getRawTextFile(R.raw.changelog))
                }
            }
        }
    }
}
