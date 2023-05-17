package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.WouldYouRatherRepository
import com.example.hub_os_device.model.WouldYouRatherResult
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WouldYouRatherViewModel @Inject constructor(private val wouldYouRatherRepository: WouldYouRatherRepository) :
    ViewModel() {

    val questionsFlow: SharedFlow<WouldYouRatherResult?> = wouldYouRatherRepository.questionFlow.map {
        it.workAroundFold(onSuccess = { question ->
            return@map question
        }, onFailure = {})
        return@map null
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)


    fun getQuest() {
        viewModelScope.launch {
            wouldYouRatherRepository.getQuestion()
        }
    }
}