package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.domain.repositories.RestaurantInfoRepository
import com.example.hub_os_device.model.Menu
import com.example.hub_os_device.ui.models.UIResult
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import com.example.hub_os_device.util.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class SelfServiceViewModel @Inject constructor(
    val deviceInfoRepository: DeviceInfoRepository,
    private val restaurantInfoRepository: RestaurantInfoRepository,
    private val sharedPreferences: SharedPreferencesHelper,
) : ViewModel() {

    init {
        getMenu()
    }

    val serviceableItemsFlow: StateFlow<UIResult> =
        deviceInfoRepository.deviceInfoState.combine(restaurantInfoRepository.menuFlow) { deviceInf, menu ->
            deviceInf.workAroundFold(onSuccess = { deviceInfo ->
                menu.workAroundFold(onSuccess = {
                    if (deviceInfo.establishment == null) return@combine UIResult.FAILURE("Oops... No establishment found.")
                    if (it == null) return@combine UIResult.FAILURE("Oops... No menu found.")
                    return@combine UIResult.SUCCESS(getServiceableItems(it))

                }, onFailure = { error -> return@combine processError(error) })
            }, onFailure = { error -> return@combine processError(error) })
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UIResult.LOADING)

    private fun processError(error: Throwable): UIResult.FAILURE {
        val errorMessage: String
        if (error is HttpException) {
            errorMessage = when (error.code()) {
                400 -> "Oops... No DineOS integration."
                404 -> "Oops... Not connected to any restaurant."
                422 -> "Oops... Could not process menu request."
                else -> "Unknown error getting menu :("
            }
            return UIResult.FAILURE(errorMessage)
        }
        return UIResult.FAILURE("Oops...${error.localizedMessage}")
    }

    val selfServiceResultFlow: SharedFlow<UIResult> = restaurantInfoRepository.selfServiceResultFlow.map {
        it.onSuccess {
            return@map UIResult.SUCCESS(it)
        }.onFailure { error ->
            val errorMessage: String
            if (error is HttpException) {
                //TODO - Go over these error messages
                errorMessage = when (error.code()) {
                    400 -> "No DineOS integration."
                    404 -> "Could not order this menu item."
                    422 -> parseError(error.response()!!).message ?: "Could not process order request"
                    else -> "Unknown error"
                }
                return@map UIResult.FAILURE(errorMessage)
            }
            return@map UIResult.FAILURE(error.localizedMessage ?: "Unknown error")
        }
        return@map UIResult.LOADING
    }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private fun getServiceableItems(
        menu: Menu?,
    ): List<ServiceableItemUIState> {
        val items = mutableListOf<ServiceableItemUIState>()

        val menuMap = mapOf("Food" to menu?.food,
            "Drinks" to menu?.drinks,
            "Shisha" to menu?.shisha,
            "Other" to menu?.other)

        menuMap.forEach {
            it.value?.forEach { menuCategory ->
                menuCategory.items.forEach { menuItem ->
                    if (menuItem.serviceable == 1) {
                        items.add(ServiceableItemUIState(menuItem.id,
                            menuItem.name,
                            menuItem.price.toInt().toString(),
                            if (!menuItem.imageUrls.isNullOrEmpty()) menuItem.imageUrls[0].optimizedImage else null))
                    }
                }
            }
        }
        return items
    }

    fun orderItem(menuItem: String) {
        val id: Int = sharedPreferences.getInt("deviceId", 0)
        val tableNum: String = sharedPreferences.getString("table_num_shared_preferences", "-1")
        viewModelScope.launch {
            restaurantInfoRepository.orderItem(id, tableNum, menuItem)
        }
    }

    fun getMenu() {
        val id: Int = sharedPreferences.getInt("deviceId", 0)
        viewModelScope.launch {
            restaurantInfoRepository.getMenu(id)
        }
    }

}

data class ServiceableItemUIState(
    val id: String,
    val name: String,
    val price: String,
    val imageUrl: String?,
)

sealed class SelfServiceStateResponseState {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    object LOADING : SelfServiceStateResponseState()
    object SUCCESS : SelfServiceStateResponseState()
    data class FAILURE(val message: String) : SelfServiceStateResponseState()
}
