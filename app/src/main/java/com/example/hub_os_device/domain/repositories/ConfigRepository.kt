package com.example.hub_os_device.domain.repositories

import com.example.hub_os_device.model.Config
import kotlinx.coroutines.flow.StateFlow

interface ConfigRepository {
    val configState: StateFlow<Result<Config?>?>
    suspend fun getConfig()
}