package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.domain.repositories.RestaurantInfoRepository
import com.example.hub_os_device.model.Menu
import com.example.hub_os_device.model.MenuCategory
import com.example.hub_os_device.ui.models.UIResult
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

enum class MenuType {
    SECTION,
    CATEGORY,
    ITEM,
}

@HiltViewModel
class MenuFragmentViewModel @Inject constructor(
    deviceInfoRepository: DeviceInfoRepository,
    private val restaurantInfoRepository: RestaurantInfoRepository,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
) :
    ViewModel() {

    init {
        getMenu()
    }

    val menuInfoFlow: SharedFlow<UIResult> =
        deviceInfoRepository.deviceInfoState.combine(restaurantInfoRepository.menuFlow) { deviceInf, menu ->
            deviceInf.workAroundFold(onSuccess = { deviceInfo ->
                val est = deviceInfo.establishment
                if (est?.dineosIntegration == true) {
                    menu.workAroundFold(onSuccess = {
                        if (it == null) {
                            return@combine UIResult.FAILURE("Oops... No menu found.")
                        }
                        return@combine UIResult.SUCCESS<MenuItemsListUIState>(MenuItemsListUIState(
                            est.name,
                            flattenMenuItems(it),
                        ))
                    }, onFailure = {
                        val errorMessage: String
                        if (it is HttpException) {
                            errorMessage = when (it.code()) {
                                400 -> "Oops... No DineOS integration."
                                404 -> "Oops... Not connected to any restaurant."
                                422 -> "Oops... Could not process menu request."
                                else -> "Unknown error getting menu :("
                            }
                            return@combine UIResult.FAILURE(errorMessage)
                        }
                        return@combine UIResult.FAILURE("Oops...${it.localizedMessage}")
                    })
                }
            }, onFailure = { error ->
                return@combine UIResult.FAILURE(error.localizedMessage ?: "Unknown Error")
            })
            return@combine UIResult.LOADING
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private fun flattenMenuItems(menu: Menu): MutableList<MenuItemUIState> {
        val uiMenu = mutableListOf<MenuItemUIState>()
        val menuMap = mapOf<String, List<MenuCategory>?>("Food" to menu.food,
            "Drinks" to menu.drinks,
            "Shisha" to menu.shisha,
            "Other" to menu.other)
        menuMap.forEach {
            if (it.value != null) uiMenu.add(MenuItemUIState(MenuType.SECTION, it.key))

            it.value?.forEach { menuCategory ->
                uiMenu.add(MenuItemUIState(MenuType.CATEGORY, menuCategory.name))

                menuCategory.items.forEach { menuItem ->
                    uiMenu.add(MenuItemUIState(MenuType.ITEM, menuItem))
                }
            }
        }
        return uiMenu
    }

    fun getMenu() {
        val id: Int =
            sharedPreferencesHelper.getInt("deviceId", 0)
        viewModelScope.launch {
            restaurantInfoRepository.getMenu(id)
        }
    }
}

data class MenuItemsListUIState
    (
    val establishmentName: String,
    val list: List<MenuItemUIState>,
    val errorMessage: String? = null,
)

data class MenuItemUIState(
    val type: MenuType,
    val item: Any,
)
