package com.genymobile.gnirehtet.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build

@SuppressLint("QueryPermissionsNeeded")
fun PackageManager.getInstalledPackagesCompat(flags: Int): List<PackageInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getInstalledPackages(flags)
    }
}

val PackageInfo.versionCodeCompat: Long
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            @Suppress("DEPRECATION") versionCode.toLong()
        }
    }

fun PackageManager.getAppIcon(activityManager: ActivityManager, applicationInfo: ApplicationInfo): Drawable? {
    val iconId = applicationInfo.icon
    val iconDpi = activityManager.launcherLargeIconDensity

    if (iconId != 0) {
        val drawable = runCatching {
            this.getResourcesForApplication(applicationInfo).getDrawableForDensity(iconId, iconDpi, null)
        }.getOrNull()

        if (drawable != null) {
            return drawable
        }
    }

    return runCatching {
        Resources.getSystem().getDrawableForDensity(android.R.mipmap.sym_def_app_icon, iconDpi, null)
    }.getOrNull()
}
