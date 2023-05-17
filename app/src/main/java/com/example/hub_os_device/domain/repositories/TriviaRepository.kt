package com.example.hub_os_device.domain.repositories

import com.example.hub_os_device.model.TriviaCategory
import com.example.hub_os_device.model.TriviaQuestion
import com.example.hub_os_device.model.TriviaToken
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface TriviaRepository {
    val triviaState: SharedFlow<Result<List<TriviaQuestion>>>
    val triviaStateQuestionProgress: SharedFlow<Int>
    val triviaToken: StateFlow<Result<String?>?>
    suspend fun getQuestions(category: TriviaCategory, token: String?)
    suspend fun retrieveToken(): String?
}