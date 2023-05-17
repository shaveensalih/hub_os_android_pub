package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class QRCodeFragmentViewModel @Inject constructor(private val deviceInfoRepository: DeviceInfoRepository) :
    ViewModel() {

    val qrCodeInfoFlow: StateFlow<QRCodeWidgetInfoUIState?> =
        deviceInfoRepository.deviceInfoState.map {
            it.workAroundFold(onSuccess = { deviceInfo ->
                val est = deviceInfo.establishment
                if (est != null)
                    return@map QRCodeWidgetInfoUIState(est.qrCode,
                        est.wifiSSID,
                        est.wifiCode,
                        est.dineosIntegration)
            }, onFailure = {})
            return@map null
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

data class QRCodeWidgetInfoUIState
    (
    val qrCode: String?,
    val wifiSSID: String?,
    val wifiCode: String?,
    val dineOSIntegrated: Boolean,
)

