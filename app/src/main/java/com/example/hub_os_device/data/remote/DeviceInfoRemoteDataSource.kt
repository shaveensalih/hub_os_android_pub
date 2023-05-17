package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.DeviceInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject

class DeviceInfoRemoteDataSource @Inject constructor(
    private val deviceInfoApi: DeviceInfoApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getDeviceInfo(id: Int): Result<DeviceInfo> =
        withContext(ioDispatcher) {
            return@withContext deviceInfoApi.getDeviceInfo(id.toString())
        }

    suspend fun registerDevice(pinCode: String): Result<DeviceInfo> =
        withContext(ioDispatcher) {
            return@withContext deviceInfoApi.registerDevice(pinCode)
        }
}

interface DeviceInfoApi {
    @GET("devices/{id}/ads/get")
    suspend fun getDeviceInfo(@Path("id") id: String): Result<DeviceInfo>

    @POST("devices/device/register")
    suspend fun registerDevice(@Query("pin_code") pinCode: String): Result<DeviceInfo>
}