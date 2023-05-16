package com.genymobile.gnirehtet.ui.views

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.ui.views.main.MainView
import com.genymobile.gnirehtet.ui.views.settings.SettingsView
import com.genymobile.gnirehtet.ui.views.settings.about.AboutChangelogView
import com.genymobile.gnirehtet.ui.views.settings.about.AboutCreditsView
import com.genymobile.gnirehtet.ui.views.settings.about.AboutLicenseView
import com.genymobile.gnirehtet.ui.views.settings.about.AboutSettingsView
import com.genymobile.gnirehtet.ui.views.settings.appearance.AppearanceSettingsView
import com.genymobile.gnirehtet.ui.views.settings.backup.BackupSettingsView
import com.genymobile.gnirehtet.ui.views.settings.general.GeneralSettingsView
import com.genymobile.gnirehtet.ui.views.settings.general.OverwriteSettingsView
import com.genymobile.gnirehtet.ui.views.setup.RequestIgnoreBatteryOptimizationsView
import com.genymobile.gnirehtet.ui.views.setup.RequestNotificationsPermissionView
import com.genymobile.gnirehtet.ui.views.setup.shouldRequestNotificationsPermission
import com.genymobile.gnirehtet.utils.ContextUtils

@Keep
sealed class Views(val id: String, val content: @Composable (NavHostController, NavBackStackEntry) -> Unit) {

    // Main view
    @Keep
    object Main : Views("main", { controller, backStack ->
        MainView(controller, backStack)
    })

    // Setup views
    @Keep
    @RequiresApi(Build.VERSION_CODES.M)
    object RequestIgnoreBatteryOptimizations : Views("ignore-battery-optimizations", { controller, backStack ->
        RequestIgnoreBatteryOptimizationsView(controller, backStack)
    })

    @Keep
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    object RequestNotificationsPermission : Views("notifications-permission", { controller, backStack ->
        RequestNotificationsPermissionView(controller, backStack)
    })

    @Keep
    // Settings views
    object Settings : Views("settings", { controller, backStack ->
        SettingsView(controller, backStack)
    })

    @Keep
    object SettingsGeneral : Views("settings-general", { controller, backStack ->
        GeneralSettingsView(controller, backStack)
    })

    @Keep
    object SettingsGeneralOverwrite : Views("settings-general-overwrite", { controller, backStack ->
        OverwriteSettingsView(controller, backStack)
    })

    @Keep
    object SettingsAppearance : Views("settings-appearance", { controller, backStack ->
        AppearanceSettingsView(controller, backStack)
    })

    @Keep
    object SettingsBackup : Views("settings-backup", { controller, backStack ->
        BackupSettingsView(controller, backStack)
    })

    @Keep
    object SettingsAbout : Views("settings-about", { controller, backStack ->
        AboutSettingsView(controller, backStack)
    })

    @Keep
    object SettingsAboutChangelog : Views("settings-about-changelog", { controller, backStack ->
        AboutChangelogView(controller, backStack)
    })

    @Keep
    object SettingsAboutLicense : Views("settings-about-license", { controller, backStack ->
        AboutLicenseView(controller, backStack)
    })

    @Keep
    object SettingsAboutCredits : Views("settings-about-credits", { controller, backStack ->
        AboutCreditsView(controller, backStack)
    })

    companion object {
        val entries by lazy {
            Views::class.sealedSubclasses.map { it.objectInstance!! }
        }

        fun getStartView(context: Context): Views {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ContextUtils.PowerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                return RequestIgnoreBatteryOptimizations
            }

            if (shouldRequestNotificationsPermission(context)) {
                return RequestNotificationsPermission
            }

            return Main
        }
    }

}
