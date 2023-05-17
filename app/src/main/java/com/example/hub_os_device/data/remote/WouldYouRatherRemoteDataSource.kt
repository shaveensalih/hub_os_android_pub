package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.WouldYouRatherResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import javax.inject.Inject

class WouldYouRatherRemoteDataSource @Inject constructor(
    private val wouldYouRatherApi: WouldYouRatherApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun getQuestion(): Result<WouldYouRatherResult> =
        withContext(ioDispatcher)
        {
            val res = wouldYouRatherApi.getQuestion()
            return@withContext res
        }
}


interface WouldYouRatherApi {
    @GET("/")
    suspend fun getQuestion(
    ): Result<WouldYouRatherResult>
}