package com.example.hub_os_device.model

data class WeatherSummary(
    val hourly: List<Weather>,
    val daily: List<Weather>
)
