package com.example.hub_os_device.domain.repositories

import com.example.hub_os_device.model.DeviceInfo
import kotlinx.coroutines.flow.SharedFlow

interface DeviceInfoRepository {
    val deviceInfoState: SharedFlow<Result<DeviceInfo>>
    val registerState: SharedFlow<Result<DeviceInfo>>
    suspend fun getDeviceInfo(id: Int)
    suspend fun registerDevice(pinCode: String)
}