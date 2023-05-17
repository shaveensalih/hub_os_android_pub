package com.example.hub_os_device.model

import com.example.hub_os_device.BuildConfig
import com.google.gson.annotations.SerializedName

data class Config(
    val socials: Socials,
    val app: App,
)

data class App(
    @SerializedName("version") val latestVersion: String,
    @SerializedName("url") val appUrl: String,
    @SerializedName("pin_code") val updatePinCode: String,
    val currentVersion: String = BuildConfig.VERSION_NAME,
)

data class Socials(
    val instagram: String?,
    val website: String?,
    @SerializedName("phone") val contact: String?,
)
