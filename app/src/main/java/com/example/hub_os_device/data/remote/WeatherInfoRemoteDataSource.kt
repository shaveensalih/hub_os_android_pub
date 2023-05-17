package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.WeatherSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject

class WeatherInfoRemoteDataSource @Inject constructor(
    private val weatherInfoApi: WeatherInfoApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getWeatherInfo(id: Int): Result<WeatherSummary> =
        withContext(ioDispatcher) {
            return@withContext weatherInfoApi.getWeatherInfo(id.toString())
        }
}

interface WeatherInfoApi {
    @GET("devices/{id}/weather/get")
    suspend fun getWeatherInfo(@Path("id") id: String): Result<WeatherSummary>
}