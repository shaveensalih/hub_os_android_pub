package com.example.hub_os_device.domain.repositories
import com.example.hub_os_device.model.WouldYouRatherResult
import kotlinx.coroutines.flow.SharedFlow

interface WouldYouRatherRepository {
    val questionFlow: SharedFlow<Result<WouldYouRatherResult>>
    suspend fun getQuestion()
}