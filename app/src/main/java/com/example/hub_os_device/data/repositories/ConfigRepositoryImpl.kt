package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.ConfigRemoteDataSource
import com.example.hub_os_device.domain.repositories.ConfigRepository
import com.example.hub_os_device.model.Config
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(private val configRemoteDataSource: ConfigRemoteDataSource) :
    ConfigRepository {

    private val _config = MutableStateFlow<Result<Config?>?>(null)
    override val configState: StateFlow<Result<Config?>?> = _config.asStateFlow()

    override suspend fun getConfig() {
        _config.update {
            configRemoteDataSource.getConfig()
        }
    }

}

