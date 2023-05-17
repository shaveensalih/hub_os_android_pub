package com.example.hub_os_device.ui.mainActivity.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.ui.RequestScheduler
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class BroadcastMessageFragmentViewModel @Inject constructor(
    deviceInfoRepository: DeviceInfoRepository,
) :
    ViewModel(), RequestScheduler {

    val broadcastMessageFlow: SharedFlow<String?> =
        deviceInfoRepository.deviceInfoState.map {
            it.workAroundFold(onSuccess = { device ->
                return@map device.tenant.broadcastMessage
            }, onFailure = {})
            return@map null
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
}