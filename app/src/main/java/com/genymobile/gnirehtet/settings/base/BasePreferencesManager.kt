package com.genymobile.gnirehtet.settings.base

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * @author Hyperion Authors, zt64
 */
abstract class BasePreferenceManager(
    private val prefs: SharedPreferences
) {
    protected fun getString(key: String, defaultValue: String) =
        prefs.getString(key, defaultValue)!!

    protected fun getBoolean(key: String, defaultValue: Boolean) = prefs.getBoolean(key, defaultValue)
    protected fun getInt(key: String, defaultValue: Int) = prefs.getInt(key, defaultValue)
    protected fun getFloat(key: String, defaultValue: Float) = prefs.getFloat(key, defaultValue)
    protected inline fun <reified E : Enum<E>> getEnum(key: String, defaultValue: E) =
        enumValueOf<E>(getString(key, defaultValue.name))

    protected fun putString(key: String, value: String) = prefs.edit { putString(key, value) }
    protected fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
    protected fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }
    protected fun putFloat(key: String, value: Float) = prefs.edit { putFloat(key, value) }
    protected inline fun <reified E : Enum<E>> putEnum(key: String, value: E) =
        putString(key, value.name)

    class Preference<T>(
        val key: String,
        val preferenceType: PreferenceType,
        private val defaultValue: T,
        val isUserSetting: Boolean = true,
        getter: (key: String, defaultValue: T) -> T,
        private val setter: (key: String, newValue: T) -> Unit
    ) {
        private val _state = MutableStateFlow(getter(key, defaultValue))
        val stateFlow: StateFlow<T> = _state

        var value: T
            get() {
                return _state.value
            }
            set(value) {
                _state.value = value
                setter(key, value)
            }

        fun reset() {
            value = defaultValue
        }
    }

    protected fun stringPreference(
        key: String,
        defaultValue: String,
        isUserSetting: Boolean = true,
    ) = Preference(
        key = key,
        preferenceType = PreferenceType.STRING,
        defaultValue = defaultValue,
        isUserSetting = isUserSetting,
        getter = ::getString,
        setter = ::putString
    )

    protected fun booleanPreference(
        key: String,
        defaultValue: Boolean,
        isUserSetting: Boolean = true,
    ) = Preference(
        key = key,
        preferenceType = PreferenceType.BOOLEAN,
        defaultValue = defaultValue,
        isUserSetting = isUserSetting,
        getter = ::getBoolean,
        setter = ::putBoolean
    )

    protected fun intPreference(
        key: String,
        defaultValue: Int,
        isUserSetting: Boolean = true,
    ) = Preference(
        key = key,
        preferenceType = PreferenceType.INT,
        defaultValue = defaultValue,
        isUserSetting = isUserSetting,
        getter = ::getInt,
        setter = ::putInt
    )

    protected fun floatPreference(
        key: String,
        defaultValue: Float,
        isUserSetting: Boolean = true,
    ) = Preference(
        key = key,
        preferenceType = PreferenceType.FLOAT,
        defaultValue = defaultValue,
        isUserSetting = isUserSetting,
        getter = ::getFloat,
        setter = ::putFloat
    )

    protected inline fun <reified E : Enum<E>> enumPreference(
        key: String,
        defaultValue: E,
        isUserSetting: Boolean = true,
    ) = Preference(
        key = key,
        preferenceType = PreferenceType.ENUM,
        defaultValue = defaultValue,
        isUserSetting = isUserSetting,
        getter = ::getEnum,
        setter = ::putEnum
    )

    enum class PreferenceType {
        BOOLEAN,
        INT,
        FLOAT,
        STRING,
        ENUM,
    }

}
