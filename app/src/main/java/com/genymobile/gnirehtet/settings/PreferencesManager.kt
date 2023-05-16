package com.genymobile.gnirehtet.settings

import android.content.Context
import android.content.SharedPreferences
import com.genymobile.gnirehtet.domain.BlockedApps
import com.genymobile.gnirehtet.domain.Gnirehtet
import com.genymobile.gnirehtet.settings.base.BasePreferenceManager
import com.genymobile.gnirehtet.ui.theme.Theme
import com.genymobile.gnirehtet.utils.toast
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmName

/**
 * @author Hyperion Authors, zt64
 */
class PreferencesManager(
    sharedPreferences: SharedPreferences
) : BasePreferenceManager(sharedPreferences) {

    // Setup
    val requestNotificationsPermission = booleanPreference("request_notifications_permission", defaultValue = true, isUserSetting = false)

    // Display
    val theme = enumPreference("theme", Theme.SYSTEM)
    val dynamicColor = booleanPreference("dynamic_color", false)

    // Gnirehtet
    private val gnirehtetDnsServers = stringPreference("gnirehtet_dns_servers", "")
    private val gnirehtetStopOnDisconnect = booleanPreference("gnirehtet_stop_on_disconnect", true)
    val gnirehtetShowToastOnConnect = booleanPreference("gnirehtet_show_toast_on_connect", true)
    val gnirehtetShowToastOnDisconnect = booleanPreference("gnirehtet_show_toast_on_disconnect", true)
    private val gnirehtetOverwriteDnsServers = booleanPreference("gnirehtet_overwrite_dns_servers", false)
    private val gnirehtetOverwriteStopOnDisconnect = booleanPreference("gnirehtet_overwrite_stop_on_disconnect", true)
    private val gnirehtetOverwriteBlockedApps = booleanPreference("gnirehtet_overwrite_blocked_apps", true)

    fun getGnirehtetDnsServers(): StateFlow<String> = gnirehtetDnsServers.stateFlow

    suspend fun setGnirehtetDnsServers(context: Context, dnsServers: String) {
        gnirehtetDnsServers.value = dnsServers

        if (Gnirehtet.getLastConfiguration()?.isStartedByServer == false || gnirehtetOverwriteDnsServers.value) {
            val old = Gnirehtet.getLastConfiguration()!!.dnsServers.toHashSet()
            val new = Gnirehtet.getCurrentDnsServers().toHashSet()
            val isSame = old.size == new.size && old.containsAll(new)

            if (!isSame) {
                Gnirehtet.restartGnirehtetIfRunning(context)
            }
        }
    }

    fun shouldStopOnDisconnect(): StateFlow<Boolean> = gnirehtetStopOnDisconnect.stateFlow

    fun setStopOnDisconnect(context: Context, stopOnDisconnect: Boolean) {
        gnirehtetStopOnDisconnect.value = stopOnDisconnect

        if (Gnirehtet.getLastConfiguration()?.isStartedByServer == false || gnirehtetOverwriteStopOnDisconnect.value) {
            Gnirehtet.getLastConfiguration()?.setStopOnDisconnect(stopOnDisconnect)

            if (stopOnDisconnect && Gnirehtet.isRunning().value && !Gnirehtet.isConnected().value) {
                Gnirehtet.stop(context)
                context.toast("Gnirehtet stopped due to no connection")
            }
        }
    }

    fun isOverwriteDnsServers(): StateFlow<Boolean> = gnirehtetOverwriteDnsServers.stateFlow

    suspend fun setOverwriteDnsServers(context: Context, overwriteDnsServers: Boolean) {
        gnirehtetOverwriteDnsServers.value = overwriteDnsServers

        if (overwriteDnsServers) {
            val old = (Gnirehtet.getLastConfiguration()?.dnsServers ?: return).toHashSet()
            val new = Gnirehtet.getCurrentDnsServers().toHashSet()
            val isSame = old.size == new.size && old.containsAll(new)

            if (!isSame) {
                Gnirehtet.restartGnirehtetIfRunning(context)
            }
        }
    }

    fun isOverwriteStopOnDisconnect(): StateFlow<Boolean> = gnirehtetOverwriteStopOnDisconnect.stateFlow

    fun setOverwriteStopOnDisconnect(overwriteStopOnDisconnect: Boolean) {
        gnirehtetOverwriteStopOnDisconnect.value = overwriteStopOnDisconnect

        if (overwriteStopOnDisconnect) {
            Gnirehtet.getLastConfiguration()?.setStopOnDisconnect(gnirehtetStopOnDisconnect.value)
        }
    }

    fun isOverwriteBlockedApps(): StateFlow<Boolean> = gnirehtetOverwriteBlockedApps.stateFlow

    suspend fun setOverwriteBlockedApps(context: Context, overwriteBlockedApps: Boolean) {
        gnirehtetOverwriteBlockedApps.value = overwriteBlockedApps

        if (overwriteBlockedApps) {
            val old = (Gnirehtet.getLastConfiguration()?.blockedPackageNames ?: return).toHashSet()
            val new = BlockedApps.getBlockedApps().toHashSet()
            val isSame = old.size == new.size && old.containsAll(new)

            if (!isSame) {
                Gnirehtet.restartGnirehtetIfRunning(context)
            }
        }
    }

    companion object {
        fun initialize(context: Context) {
            Preferences = PreferencesManager(context.getSharedPreferences("preferences", Context.MODE_PRIVATE))
        }

        val allPreferences by lazy {
            PreferencesManager::class.declaredMemberProperties.mapNotNull {
                it.isAccessible = true
                it.get(Preferences) as? Preference<*>
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun import(context: Context, jsonString: String): Boolean {
        val jsonObject = JSONObject(jsonString)
        var successful = false

        for (preference in allPreferences) {
            if (!preference.isUserSetting) {
                continue
            }

            try {
                when (preference.preferenceType) {
                    PreferenceType.BOOLEAN -> {
                        (preference as Preference<Boolean>).value =
                            jsonObject.getBoolean(preference.key)
                    }

                    PreferenceType.INT -> {
                        (preference as Preference<Int>).value = jsonObject.getInt(preference.key)
                    }

                    PreferenceType.FLOAT -> {
                        (preference as Preference<Float>).value =
                            jsonObject.getDouble(preference.key).toFloat()
                    }

                    PreferenceType.STRING -> {
                        (preference as Preference<String>).value =
                            jsonObject.getString(preference.key)
                    }
                    PreferenceType.ENUM -> {
                        val enumConstants = Class.forName(jsonObject.getString("__enum_class_${preference.key}"))
                            .enumConstants?.filterIsInstance(Enum::class.java) ?: continue
                        val enumName = jsonObject.getString(preference.key)
                        val enumValue = enumConstants.firstOrNull { it.name == enumName } ?: continue
                        (preference as Preference<Enum<*>>).value = enumValue
                    }
                }

                successful = true
            } catch (ignore: JSONException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            val blockedApps = jsonObject.getJSONArray("__blocked_apps")
            val packageNames = mutableListOf<String>()

            for (i in 0 until blockedApps.length()) {
                val blockedName = blockedApps.get(i) as? String ?: continue
                packageNames.add(blockedName)
            }

            BlockedApps.import(packageNames)
            successful = true
        } catch (ignore: JSONException) {}

        Gnirehtet.restartGnirehtetIfRunning(context)

        return successful
    }

    fun export(): String {
        val jsonObject = JSONObject()

        for (preference in allPreferences) {
            if (!preference.isUserSetting) {
                continue
            }

            when (preference.preferenceType) {
                PreferenceType.BOOLEAN -> {
                    jsonObject.put(preference.key, preference.value as Boolean)
                }

                PreferenceType.INT -> {
                    jsonObject.put(preference.key, preference.value as Int)
                }

                PreferenceType.FLOAT -> {
                    jsonObject.put(preference.key, (preference.value as Float).toDouble())
                }

                PreferenceType.STRING -> {
                    jsonObject.put(preference.key, preference.value as String)
                }
                PreferenceType.ENUM -> {
                    jsonObject.put("__enum_class_${preference.key}", preference.value!!::class.jvmName)
                    jsonObject.put(preference.key, (preference.value as Enum<*>).name)
                }
            }
        }

        val blockedApps = JSONArray()

        for (blockedApp in BlockedApps.getBlockedApps()) {
            blockedApps.put(blockedApp)
        }

        jsonObject.put("__blocked_apps", blockedApps)

        return jsonObject.toString()
    }

    suspend fun reset(context: Context) {
        for (preference in allPreferences) {
            if (!preference.isUserSetting) {
                continue
            }

            preference.reset()
        }

        BlockedApps.reset()
        Gnirehtet.restartGnirehtetIfRunning(context)
    }

}

lateinit var Preferences: PreferencesManager
