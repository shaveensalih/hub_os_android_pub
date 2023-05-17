package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.DailyHoroscope
import com.example.hub_os_device.model.StarSign
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject

class HoroscopeRemoteDataSource @Inject constructor(
    private val horoscopeApi: HoroscopeApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun getHoroscope(
        sign: StarSign,
    ): Result<DailyHoroscope> =
        withContext(ioDispatcher)
        {
            val res = horoscopeApi.getHoroscope(sign.value)
            println("in res $res")
            return@withContext res
        }
}


interface HoroscopeApi {
    @POST("/")
    suspend fun getHoroscope(
        @Query("sign") sign: String,
        @Query("day") day: String = "today",
    ): Result<DailyHoroscope>
}