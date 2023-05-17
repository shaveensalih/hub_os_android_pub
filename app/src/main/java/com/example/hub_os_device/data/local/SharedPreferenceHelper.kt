package com.example.hub_os_device.data.local

interface SharedPreferencesHelper {
    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String
    fun putInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int): Int
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
}
