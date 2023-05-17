package com.example.hub_os_device.ui.mainActivity.viewModel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisplayWidgetsViewModel @Inject constructor(private val sharedPreferences: SharedPreferencesHelper) :
    ViewModel() {

    private val _dayNightModeFlow = MutableStateFlow<Boolean?>(null)
    val dayNightModeFlow: StateFlow<Boolean?> = _dayNightModeFlow.asStateFlow()

    init {
        changeDayNightMode(sharedPreferences.getBoolean("dayNight", true))
    }

    fun changeDayNightMode(isNightMode: Boolean) {
        viewModelScope.launch {
            _dayNightModeFlow.emit(isNightMode)
            sharedPreferences.putBoolean("dayNight", isNightMode)
        }
    }
}