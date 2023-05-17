package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.DeviceInfoRemoteDataSource
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.model.DeviceInfo
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class DeviceInfoRepositoryImpl @Inject constructor(private val deviceInfoRemoteDataSource: DeviceInfoRemoteDataSource) :
    DeviceInfoRepository {

    private val _deviceInfo = MutableSharedFlow<Result<DeviceInfo>>(replay = 1)
    private val _registerState = MutableSharedFlow<Result<DeviceInfo>>(replay = 1)
    override val deviceInfoState: SharedFlow<Result<DeviceInfo>> = _deviceInfo.asSharedFlow()
    override val registerState: SharedFlow<Result<DeviceInfo>> = _registerState.asSharedFlow()

    override suspend fun getDeviceInfo(id: Int) {
        return _deviceInfo.emit(deviceInfoRemoteDataSource.getDeviceInfo(id))
    }

    override suspend fun registerDevice(pinCode: String) {
        val registerState = deviceInfoRemoteDataSource.registerDevice(pinCode)
        _registerState.emit(registerState)
        _deviceInfo.emit(registerState)
        return
    }

}

