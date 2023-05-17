package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import javax.inject.Inject

class TopicOfDayRemoteDataSource @Inject constructor(
    private val topicOfDayApi: TopicOfDayApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getTopicOfDay(): Result<List<String>> =
        withContext(ioDispatcher) {
            return@withContext topicOfDayApi.getTopicInfo()
        }
}

interface TopicOfDayApi {
    @GET("apps/topics/get")
    suspend fun getTopicInfo(): Result<List<String>>
}