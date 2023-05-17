package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("weather_code") val code: Int,
    val timestamp: String,
    @SerializedName("temperature") val temp: Double,
    @SerializedName("description") val desc: String
)
