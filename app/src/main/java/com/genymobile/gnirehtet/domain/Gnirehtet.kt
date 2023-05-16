package com.genymobile.gnirehtet.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.genymobile.gnirehtet.*
import com.genymobile.gnirehtet.settings.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import kotlin.time.Duration.Companion.milliseconds

object Gnirehtet {

    private const val TAG = "Gnirehtet"
    private var lastRestartGnirehtetJob: Job? = null

    fun createConfig(
        intentDnsServers: Array<InetAddress>? = null,
        intentRoutes: Array<CIDR>? = null,
        intentBlockedPackageNames: Array<String>? = null,
        intentStopOnDisconnect: Boolean? = null,
        startedByServer: Boolean = false,
    ): VpnConfiguration {
        val dnsServers = if (intentDnsServers != null && !Preferences.isOverwriteDnsServers().value) {
            intentDnsServers
        } else {
            getCurrentDnsServers()
        }

        val routes = intentRoutes ?: arrayOf()
        val blockedPackageNames = if (intentBlockedPackageNames != null && !Preferences.isOverwriteBlockedApps().value) {
            intentBlockedPackageNames
        } else {
            BlockedApps.getBlockedApps().toList().toTypedArray()
        }
        val stopOnDisconnect = if (intentStopOnDisconnect != null && !Preferences.isOverwriteStopOnDisconnect().value) {
            intentStopOnDisconnect
        } else {
            Preferences.shouldStopOnDisconnect().value
        }

        return VpnConfiguration(
            dnsServers,
            routes,
            blockedPackageNames,
            stopOnDisconnect,
            startedByServer,
        )
    }

    fun getCurrentDnsServers(): Array<out InetAddress> {
        return Preferences.getGnirehtetDnsServers().value
            .split(',')
            .filter { it.isNotBlank() }
            .mapNotNull { address ->
                val formattedAddress = address.trim()

                try {
                    Net.toInetAddress(formattedAddress)
                } catch (_: IllegalArgumentException) {
                    Log.w(TAG, "Failed to parse dns server: $formattedAddress")
                    null
                }
            }.toTypedArray()
    }

    @Composable
    fun createVpnRequestReceiver(context: Context): ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                GnirehtetService.start(context, createConfig())
            }
        }
    }

    fun start(context: Context, receiver: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val vpnIntent = startWithNoVpnRequest(context) ?: return
        Log.w(TAG, "VPN requires the authorization from the user, requesting...")
        receiver.launch(vpnIntent)
    }

    private fun startWithNoVpnRequest(context: Context): Intent? {
        val vpnIntent = VpnService.prepare(context)

        if (vpnIntent == null) {
            Log.d(TAG, "VPN was already authorized")
            // we got the permission, start the service now
            GnirehtetService.start(context, createConfig())
            return null
        }

        return vpnIntent
    }

    fun stop(context: Context) {
        GnirehtetService.stop(context)
    }

    fun launchRestartGnirehtetScope(coroutineScope: CoroutineScope, block: suspend CoroutineScope.() -> Unit) {
        lastRestartGnirehtetJob?.also { job ->
            runBlocking {
                job.cancelAndJoin()
            }
        }

        lastRestartGnirehtetJob = coroutineScope.launch(Dispatchers.IO) {
            block(this)
        }
    }

    suspend fun restartGnirehtetIfRunning(context: Context) {
        if (isRunning().value) {
            stop(context)

            val start = System.currentTimeMillis()

            while (System.currentTimeMillis() - start < 1000) {
                if (!isRunning().value) {
                    startWithNoVpnRequest(context)
                    break
                }

                delay(10.milliseconds)
            }
        }
    }

    fun isRunning(): StateFlow<Boolean> {
        return GnirehtetService.isRunning()
    }

    fun isConnected(): StateFlow<Boolean> {
        return GnirehtetService.isConnected()
    }

    fun getLastConfiguration(): VpnConfiguration? {
        return GnirehtetService.getLastConfiguration()
    }

}
