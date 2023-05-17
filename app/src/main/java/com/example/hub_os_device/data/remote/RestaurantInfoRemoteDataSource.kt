package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.Menu
import com.example.hub_os_device.model.OrderResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject

class RestaurantInfoRemoteDataSource @Inject constructor(
    private val restaurantInfoApi: RestaurantInfoApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getMenu(id: Int): Result<Menu> =
        withContext(ioDispatcher) {
            return@withContext restaurantInfoApi.getMenu(id.toString())
        }

    suspend fun orderItem(id: Int, tableNumber: String, menuItem: String): Result<OrderResult> =
        withContext(ioDispatcher) {
            return@withContext restaurantInfoApi.orderItem(id.toString(), tableNumber, menuItem)
        }
}

interface RestaurantInfoApi {
    @GET("devices/{id}/menu/get")
    suspend fun getMenu(@Path("id") id: String): Result<Menu>

    @POST("devices/{id}/order/self")
    suspend fun orderItem(@Path("id") id: String, @Query("tableNumber") tableNumber: String, @Query("menuItem") menuItem: String): Result<OrderResult>

}