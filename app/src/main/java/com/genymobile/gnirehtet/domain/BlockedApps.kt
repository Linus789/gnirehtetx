package com.genymobile.gnirehtet.domain

import android.content.Context
import com.genymobile.gnirehtet.settings.Preferences
import com.genymobile.gnirehtet.utils.ContextUtils
import java.io.File

object BlockedApps {

    private val blockedAppsFile by lazy {
       File(ContextUtils.AssetsDir, "blocked_apps")
    }

    private val blockedApps by lazy {
        val set = HashSet<String>()

        if (blockedAppsFile.exists()) {
            blockedAppsFile.forEachLine { packageName ->
                set.add(packageName)
            }
        }

        set
    }

    fun getBlockedApps(): Iterable<String> {
        return blockedApps.asIterable()
    }

    fun isAppBlocked(packageName: String): Boolean {
        return blockedApps.contains(packageName)
    }

    fun import(blockedApps: List<String>) {
        this.blockedApps.clear()
        this.blockedApps.addAll(blockedApps)
        writeFile()
    }

    fun reset() {
        blockedApps.clear()
        writeFile()
    }

    suspend fun setAppBlocked(context: Context, packageName: String, blocked: Boolean) {
        val changed = if (blocked) {
            blockedApps.add(packageName)
        } else {
            blockedApps.remove(packageName)
        }

        if (!changed) {
            return
        }

        writeFile()

        if (Gnirehtet.getLastConfiguration()?.isStartedByServer == false || Preferences.isOverwriteBlockedApps().value) {
            Gnirehtet.restartGnirehtetIfRunning(context)
        }
    }

    private fun writeFile() {
        blockedAppsFile.writeText(blockedApps.joinToString(separator = "\n"))
    }

}
