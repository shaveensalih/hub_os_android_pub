package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.HoroscopeRemoteDataSource
import com.example.hub_os_device.domain.repositories.HoroscopeRepository
import com.example.hub_os_device.model.DailyHoroscope
import com.example.hub_os_device.model.StarSign
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class HoroscopeRepositoryImpl @Inject constructor(private val horoscopeRemoteDataSource: HoroscopeRemoteDataSource) :
    HoroscopeRepository {

    private val _horoscopeFlow = MutableSharedFlow<Result<DailyHoroscope>>(replay = 1)
    override val horoscopeFlow: SharedFlow<Result<DailyHoroscope>> =
        _horoscopeFlow.asSharedFlow()

    override suspend fun getHoroscope(sign: StarSign) {
        return _horoscopeFlow.emit(horoscopeRemoteDataSource.getHoroscope(sign))
    }

}

