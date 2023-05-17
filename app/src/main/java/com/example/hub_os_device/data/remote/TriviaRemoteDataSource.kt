package com.example.hub_os_device.data.remote

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.model.Difficulty
import com.example.hub_os_device.model.TriviaCategory
import com.example.hub_os_device.model.TriviaResponse
import com.example.hub_os_device.model.TriviaToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

class TriviaRemoteDataSource @Inject constructor(
    private val triviaApi: TriviaInfoApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun getQuestion(
        difficulty: Difficulty,
        category: TriviaCategory,
        token: String,
    ): Result<TriviaResponse> =
        withContext(ioDispatcher)
        {
            val res = triviaApi.getQuestion(difficulty.value, category.value.toInt(), token = token)
            println(res)
            return@withContext res
        }

    suspend fun retrieveToken(): Result<TriviaToken> =
        withContext(ioDispatcher) {
            return@withContext triviaApi.retrieveToken()
        }

    suspend fun resetToken(token: String): Result<TriviaToken> =
        withContext(ioDispatcher) {
            return@withContext triviaApi.resetToken(token)
        }
}


interface TriviaInfoApi {
    @GET("api.php")
    suspend fun getQuestion(
        @Query("difficulty") difficulty: String,
        @Query("category") category: Int,
        @Query("type") type: String = "multiple",
        @Query("amount") amount: Int = 1,
        @Query("token") token: String,
    ): Result<TriviaResponse>

    @GET("api_token.php?command=request")
    suspend fun retrieveToken(): Result<TriviaToken>

    @GET("api_token.php?command=reset")
    suspend fun resetToken(@Query("token") token: String): Result<TriviaToken>
}