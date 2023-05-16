package com.genymobile.gnirehtet.utils

import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RawRes
import androidx.core.content.getSystemService
import com.genymobile.gnirehtet.settings.PreferencesManager
import java.io.File

object ContextUtils {

    lateinit var AssetsDir: File
    lateinit var PackageManager: PackageManager
    lateinit var PowerManager: PowerManager
    lateinit var ActivityManager: ActivityManager

    fun initialize(context: Context) {
        AssetsDir = context.filesDir.also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        PackageManager = context.packageManager!!
        PowerManager = context.getSystemService()!!
        ActivityManager = context.getSystemService()!!

        PreferencesManager.initialize(context)
    }

}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun Context.toast(string: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, string, duration).show()
}

fun Resources.getRawTextFile(@RawRes id: Int) =
    openRawResource(id).bufferedReader().use { it.readText() }
