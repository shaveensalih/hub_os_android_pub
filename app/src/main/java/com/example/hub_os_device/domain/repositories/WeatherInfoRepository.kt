package com.example.hub_os_device.domain.repositories

import com.example.hub_os_device.model.WeatherSummary
import kotlinx.coroutines.flow.SharedFlow

interface WeatherInfoRepository {
    val weatherInfoState: SharedFlow<Result<WeatherSummary>>
    suspend fun getWeatherInfo(deviceId: Int)
}