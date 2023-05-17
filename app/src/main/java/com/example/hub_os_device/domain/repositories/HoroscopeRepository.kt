package com.example.hub_os_device.domain.repositories

import com.example.hub_os_device.model.DailyHoroscope
import com.example.hub_os_device.model.StarSign
import kotlinx.coroutines.flow.SharedFlow

interface HoroscopeRepository {
    val horoscopeFlow: SharedFlow<Result<DailyHoroscope>>
    suspend fun getHoroscope(sign: StarSign)
}