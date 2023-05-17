package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.WeatherInfoRemoteDataSource
import com.example.hub_os_device.domain.repositories.WeatherInfoRepository
import com.example.hub_os_device.model.WeatherSummary
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class WeatherInfoRepositoryImpl @Inject constructor(private val weatherInfoRemoteDataSource: WeatherInfoRemoteDataSource) :
    WeatherInfoRepository {

    private val _weatherInfo = MutableSharedFlow<Result<WeatherSummary>>(replay = 1)
    override val weatherInfoState: SharedFlow<Result<WeatherSummary>> = _weatherInfo.asSharedFlow()

    override suspend fun getWeatherInfo(deviceId: Int) {
        return _weatherInfo.emit(weatherInfoRemoteDataSource.getWeatherInfo(deviceId))
    }

}

