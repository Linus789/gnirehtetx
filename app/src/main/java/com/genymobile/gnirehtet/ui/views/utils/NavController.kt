package com.genymobile.gnirehtet.ui.views.utils

import androidx.navigation.NavHostController
import com.genymobile.gnirehtet.ui.views.Views

fun NavHostController.navigate(route: Views) = this.navigate(route.id)

fun NavHostController.navigateNoReturn(route: Views) {
    navigate(route.id) {
        popUpTo(
            currentBackStackEntry?.destination?.route ?: return@navigate
        ) {
            inclusive = true
        }
    }
}
