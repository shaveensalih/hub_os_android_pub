package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.TopicOfDayRemoteDataSource
import com.example.hub_os_device.domain.repositories.TopicOfDayRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class TopicOfDayRepositoryImpl @Inject constructor(private val topicOfDayRemoteDataSource: TopicOfDayRemoteDataSource) :
    TopicOfDayRepository {

    private val _topicOfDayState = MutableSharedFlow<Result<List<String>>>(replay = 1)
    override val topicOfDayState: SharedFlow<Result<List<String>>> = _topicOfDayState.asSharedFlow()

    override suspend fun getTopicOfDay() {
        return _topicOfDayState.emit(topicOfDayRemoteDataSource.getTopicOfDay())
    }

}

