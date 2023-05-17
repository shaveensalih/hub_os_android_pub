package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.RestaurantInfoRemoteDataSource
import com.example.hub_os_device.domain.repositories.RestaurantInfoRepository
import com.example.hub_os_device.model.Menu
import com.example.hub_os_device.model.OrderResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class RestaurantInfoRepositoryImpl @Inject constructor(private val restaurantInfoRemoteDataSource: RestaurantInfoRemoteDataSource) :
    RestaurantInfoRepository {

    private val _menuFlow = MutableSharedFlow<Result<Menu?>>(1)
    override val menuFlow: SharedFlow<Result<Menu?>> = _menuFlow.asSharedFlow()

    private val _selfServiceResultFlow = MutableSharedFlow<Result<OrderResult?>>(1)
    override val selfServiceResultFlow: SharedFlow<Result<OrderResult?>> = _selfServiceResultFlow.asSharedFlow()

    override suspend fun getMenu(id: Int) {
        return _menuFlow.emit(restaurantInfoRemoteDataSource.getMenu(id))
    }

    override suspend fun orderItem(id: Int, tableNumber: String, menuItemId: String) {
        return _selfServiceResultFlow.emit(restaurantInfoRemoteDataSource.orderItem(id,
            tableNumber,
            menuItemId))
    }

}

