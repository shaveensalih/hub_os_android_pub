package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.HoroscopeRepository
import com.example.hub_os_device.model.DailyHoroscope
import com.example.hub_os_device.model.StarSign
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HoroscopeViewModel @Inject constructor(private val horoscopeRepository: HoroscopeRepository) :
    ViewModel() {

    private val _horoscopeFlow:
            SharedFlow<Result<DailyHoroscope>> = horoscopeRepository.horoscopeFlow
    val horoscopeFlow: SharedFlow<HoroscopeState?> = _horoscopeFlow.map {
        it.workAroundFold(onSuccess = { horoscope ->
            return@map HoroscopeState.SUCCESS(horoscope)
        }, onFailure = { error ->
            return@map HoroscopeState.FAILURE(error.message ?: "Unknown Error")
        })
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    fun getHoroscope(sign: StarSign) {
        viewModelScope.launch {
            horoscopeRepository.getHoroscope(sign)
        }
    }
}

sealed class HoroscopeState {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    object LOADING : HoroscopeState()
    data class SUCCESS(val horoscope: DailyHoroscope) : HoroscopeState()
    data class FAILURE(val message: String) : HoroscopeState()
}