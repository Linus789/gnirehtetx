package com.genymobile.gnirehtet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.ui.theme.GnirehtetXTheme
import com.genymobile.gnirehtet.ui.views.Views
import com.genymobile.gnirehtet.utils.ContextUtils

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtils.initialize(this)

        setContent {
            val theme by Preferences.theme.stateFlow.collectAsState()
            val dynamicColor by Preferences.dynamicColor.stateFlow.collectAsState()

            GnirehtetXTheme(
                theme = theme,
                dynamicColor = dynamicColor,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = 0.dp),
                        navController = navController,
                        startDestination = Views.getStartView(this).id,
                    ) {
                        for (view in Views.entries) {
                            composable(view.id) {
                                view.content(navController, it)
                            }
                        }
                    }
                }
            }
        }
    }

}
