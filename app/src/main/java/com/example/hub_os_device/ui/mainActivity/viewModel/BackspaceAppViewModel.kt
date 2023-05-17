package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.ConfigRepository
import com.example.hub_os_device.model.Socials
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class BackspaceAppViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) :
    ViewModel() {

    val linksFlow: SharedFlow<Socials> = configRepository.configState.map {
        it.workAroundFold(onSuccess = { config ->
            return@map config.socials
        }, onFailure = {})
        return@map Socials("http://www.instagram.com/backspace.krd",
            "https://backspace.krd",
            "mailto:shad@backspace.com")
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

}


