package com.genymobile.gnirehtet.ui.views.setup

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.ui.views.utils.SelectButton
import com.genymobile.gnirehtet.ui.views.utils.navigateNoReturn
import com.genymobile.gnirehtet.utils.ContextUtils
import com.genymobile.gnirehtet.ui.views.Views

@SuppressLint("BatteryLife")
@Composable
@RequiresApi(Build.VERSION_CODES.M)
fun RequestIgnoreBatteryOptimizationsView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val receiver = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (ContextUtils.PowerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            navController.navigateNoReturn(Views.getStartView(context))
        }
    }

    SetupView {
        SelectButton(text = "Disable battery optimizations") {
            receiver.launch(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "To have a higher chance of a stable connection, please disable the battery optimizations.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
