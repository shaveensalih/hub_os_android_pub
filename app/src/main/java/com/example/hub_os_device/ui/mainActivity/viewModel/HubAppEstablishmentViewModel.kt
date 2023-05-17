package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.ui.models.UIResult
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class EstablishmentViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val sharedPreferences: SharedPreferencesHelper,
    private val firebase: Firebase
) : ViewModel() {

    private var currentPassword: String? = null

    val establishmentFlow: SharedFlow<UIResult> =
        deviceInfoRepository.deviceInfoState.map {
            it.workAroundFold(onSuccess = { deviceInfo ->
                val est = deviceInfo.establishment
                if (est != null) {
                    currentPassword = est.passcode
                    firebase.analytics.setUserProperty("establishment", est.name)
                    return@map UIResult.SUCCESS(EstablishmentUIState(est.name,
                        est.passcode,
                        null,
                        est.tableCount,
                        est.dineosIntegration))
                }

            }, onFailure = { error ->
                val errorMessage: String
                if (error is HttpException) {
                    errorMessage = when (error.code()) {
                        404 -> "Oops... Not found."
                        422 -> "Oops... Could not process request."
                        else -> "Unknown error :("
                    }
                    return@map UIResult.FAILURE(errorMessage)
                }
                return@map UIResult.FAILURE("Oops...${error.localizedMessage}")
            })
            return@map UIResult.LOADING
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val dineOSIntegrationFlow: StateFlow<Boolean> = establishmentFlow.map {
        when (it) {
            is UIResult.SUCCESS<*> -> return@map (it.value as EstablishmentUIState).dineosIntegration
            else -> return@map false
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun checkPinCodesMatch(enteredPinCode: String): Boolean {
        return enteredPinCode == currentPassword
    }

    fun reloadAds() {
        viewModelScope.launch {
            val id: Int = sharedPreferences.getInt("deviceId", 0)
            deviceInfoRepository.getDeviceInfo(id)
        }
    }
}

data class EstablishmentUIState
    (
    val name: String,
    val pinCode: String,
    val deviceId: String?,
    val tableCount: Int?,
    val dineosIntegration: Boolean,
)

