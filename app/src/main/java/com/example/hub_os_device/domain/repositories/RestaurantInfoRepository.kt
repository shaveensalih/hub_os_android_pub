package com.example.hub_os_device.domain.repositories

import com.example.hub_os_device.model.Menu
import com.example.hub_os_device.model.OrderResult
import kotlinx.coroutines.flow.SharedFlow

interface RestaurantInfoRepository {
    val menuFlow: SharedFlow<Result<Menu?>>
    suspend fun getMenu(id: Int)
    val selfServiceResultFlow: SharedFlow<Result<OrderResult?>>
    suspend fun orderItem(id: Int, tableNumber: String, menuItemId: String)
}