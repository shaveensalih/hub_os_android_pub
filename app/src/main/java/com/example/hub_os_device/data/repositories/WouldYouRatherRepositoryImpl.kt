package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.WouldYouRatherRemoteDataSource
import com.example.hub_os_device.domain.repositories.WouldYouRatherRepository
import com.example.hub_os_device.model.WouldYouRatherResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class WouldYouRatherRepositoryImpl @Inject constructor(private val wouldYouRatherRemoteDataSource: WouldYouRatherRemoteDataSource) :
    WouldYouRatherRepository {

    private val _questionFlow = MutableSharedFlow<Result<WouldYouRatherResult>>(replay = 1)
    override val questionFlow: SharedFlow<Result<WouldYouRatherResult>> =
        _questionFlow.asSharedFlow()

    override suspend fun getQuestion() {
        return _questionFlow.emit(wouldYouRatherRemoteDataSource.getQuestion())
    }

}

