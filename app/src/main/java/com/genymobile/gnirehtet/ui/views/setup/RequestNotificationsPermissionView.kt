package com.genymobile.gnirehtet.ui.views.setup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.ui.views.Views
import com.genymobile.gnirehtet.ui.views.utils.SelectButton
import com.genymobile.gnirehtet.ui.views.utils.navigateNoReturn

fun shouldRequestNotificationsPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return false
    }

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        return false
    }

    if (!Preferences.requestNotificationsPermission.value) {
        return false
    }

    return true
}

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun RequestNotificationsPermissionView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val receiver = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        Preferences.requestNotificationsPermission.value = false
        navController.navigateNoReturn(Views.getStartView(context))
    }

    SetupView {
        SelectButton(text = "Ask for notifications permission") {
            receiver.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "If you want to be able to control Gnirehtet from the notifications, please grant the notifications permission.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
