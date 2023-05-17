package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.domain.repositories.RestaurantInfoRepository
import com.example.hub_os_device.model.Menu
import com.example.hub_os_device.model.MenuCategory
import com.example.hub_os_device.model.MenuItem
import com.example.hub_os_device.model.OrderResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeRestaurantInfoRepository(
) : RestaurantInfoRepository {

    private
    var shouldReturnNetworkError: Boolean = false

    private
    var httpExceptionCode: Int? = null

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    fun setHttpExceptionCode(value: Int) {
        httpExceptionCode = value
    }

    fun setMenu(value: Menu?) {
        menu = value
    }

    private val _menuFlow: MutableSharedFlow<Result<Menu?>> = MutableSharedFlow<Result<Menu?>>(1)
    override val menuFlow: SharedFlow<Result<Menu?>> = _menuFlow.asSharedFlow()

    override suspend fun getMenu(id: Int) {
        if (shouldReturnNetworkError) {
            _menuFlow.emit(Result.failure(IOException("Network Exception")))
        } else if (httpExceptionCode != null)
            _menuFlow.emit(Result.failure(HttpException(Response.error<HttpException>(
                httpExceptionCode!!,
                ResponseBody.create(
                    null, "{}")))))
        else _menuFlow.emit(Result.success(menu))
    }

    private val _selfServiceResultFlow: MutableSharedFlow<Result<OrderResult?>> = MutableSharedFlow<Result<OrderResult?>>(1)
    override val selfServiceResultFlow: SharedFlow<Result<OrderResult?>> = _selfServiceResultFlow.asSharedFlow()

    override suspend fun orderItem(id: Int, tableNumber: String, menuItemId: String) {
        if (shouldReturnNetworkError) {
            _selfServiceResultFlow.emit(Result.failure(IOException("Network Exception")))
        } else if (httpExceptionCode != null)
            _selfServiceResultFlow.emit(Result.failure(HttpException(Response.error<HttpException>(
                httpExceptionCode!!,
                ResponseBody.create(
                    null, "{}")))))
        else _selfServiceResultFlow.emit(Result.success(OrderResult("Successful")))
    }

    private var menu: Menu? =
        Menu(listOf(MenuCategory(
            "1",
            "Pasta",
            null,
            listOf(
                MenuItem(
                    "1",
                    "Pasta Chow Mein",
                    "jwhkwehf kwefkajwenfkjaef kajwefkjaenf",
                    1000.0,
                    null,
                    null,
                    1,
                    1,
                    null
                ),
                MenuItem(
                    "2",
                    "Pasty food food",
                    "Hello this is what food looks like hello are you",
                    5000.0,
                    null,
                    null,
                    1,
                    1,
                    null
                ),
                MenuItem(
                    "3",
                    "Some other pasta",
                    "What are you doing this is pasta just order it.",
                    5000.0,
                    null,
                    null,
                    1,
                    1,
                    null
                ),
            ),
            1
        )), listOf(), listOf(), listOf())
}