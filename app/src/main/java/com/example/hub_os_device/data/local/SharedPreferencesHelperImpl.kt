package com.example.hub_os_device.data.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import javax.inject.Inject

class SharedPreferencesHelperImpl @Inject constructor(application: Application) :
    SharedPreferencesHelper {
    private val tableSharedPreferences =
        application.applicationContext.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)
    private val deviceSharedPreferences =
        application.applicationContext.getSharedPreferences("deviceIdPref", Context.MODE_PRIVATE)
    private val defaultSharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)

    private val sharedPreferencesMap = mapOf<String, SharedPreferences>(
        "deviceId" to deviceSharedPreferences,
        "table_num_shared_preferences" to tableSharedPreferences,
        "dayNight" to defaultSharedPreferences
    )

    override fun putString(key: String, value: String) {
        (sharedPreferencesMap[key] ?: defaultSharedPreferences).edit().putString(key, value).apply()
    }

    override fun getString(key: String, defaultValue: String): String {
        return (sharedPreferencesMap[key] ?: defaultSharedPreferences).getString(key, defaultValue)
            ?: defaultValue
    }

    override fun putInt(key: String, value: Int) {
        (sharedPreferencesMap[key] ?: defaultSharedPreferences).edit().putInt(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return (sharedPreferencesMap[key] ?: defaultSharedPreferences).getInt(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        (sharedPreferencesMap[key] ?: defaultSharedPreferences).edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return (sharedPreferencesMap[key] ?: defaultSharedPreferences).getBoolean(key, defaultValue)
    }

    fun getSharedPreference(key: String): SharedPreferences {
        return sharedPreferencesMap[key] ?: defaultSharedPreferences
    }
}