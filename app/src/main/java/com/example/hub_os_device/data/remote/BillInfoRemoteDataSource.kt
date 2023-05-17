package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.BillInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject

class BillInfoRemoteDataSource @Inject constructor(
    private val billInfoApi: BillInfoApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getBillInfo(id: Int, tableNumber: String): Result<BillInfo?> =
        withContext(ioDispatcher) {
            return@withContext billInfoApi.getBillInfo(id.toString(), tableNumber)
        }
}

interface BillInfoApi {
    @GET("devices/{id}/order/get")
    suspend fun getBillInfo(@Path("id") id: String, @Query("tableNumber") tableNumber: String): Result<BillInfo?>
}