package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.Config
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import javax.inject.Inject

class ConfigRemoteDataSource @Inject constructor(
    private val configApi: ConfigApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getConfig(): Result<Config?> =
        withContext(ioDispatcher) {
            return@withContext configApi.getConfig()
        }

}

interface ConfigApi {
    @GET("devices/device/config/get")
    suspend fun getConfig(): Result<Config?>
}