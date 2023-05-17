package com.example.hub_os_device.domain.repositories

import kotlinx.coroutines.flow.SharedFlow

interface TopicOfDayRepository {
    val topicOfDayState: SharedFlow<Result<List<String>>>
    suspend fun getTopicOfDay()
}