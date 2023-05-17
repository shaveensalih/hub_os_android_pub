package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.di.IoDispatcher
import com.example.hub_os_device.data.remote.TriviaRemoteDataSource
import com.example.hub_os_device.domain.repositories.TriviaRepository
import com.example.hub_os_device.model.Difficulty
import com.example.hub_os_device.model.TriviaCategory
import com.example.hub_os_device.model.TriviaQuestion
import com.example.hub_os_device.model.TriviaResponse
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TriviaRepositoryImpl @Inject constructor(
    private val triviaRemoteDataSource: TriviaRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TriviaRepository {

    private val _triviaState = MutableSharedFlow<Result<List<TriviaQuestion>>>(1)
    override val triviaState: SharedFlow<Result<List<TriviaQuestion>>> = _triviaState.asSharedFlow()
    private val _triviaStateQuestionProgress = MutableSharedFlow<Int>(1)
    override val triviaStateQuestionProgress: SharedFlow<Int> = _triviaStateQuestionProgress.asSharedFlow()
    private val _triviaToken = MutableStateFlow<Result<String?>?>(null)
    override val triviaToken: StateFlow<Result<String?>?> = _triviaToken.asStateFlow()

    override suspend fun getQuestions(category: TriviaCategory, token: String?) {
        withContext(ioDispatcher) {
            if (token == null) {
                try {
                    val tokenAsyncCall = async { retrieveToken() }
                    manageQuestions(category, tokenAsyncCall.await()!!)
                } catch (e: Exception) {
                    println(e)
                    _triviaState.emit(Result.failure(e))
                }
            } else {
                manageQuestions(category, token)
            }
        }
    }

    private suspend fun manageQuestions(category: TriviaCategory, token: String) =
        withContext(ioDispatcher)
        {
            val questions: MutableList<TriviaQuestion> = mutableListOf<TriviaQuestion>()
            var questionCount = 0
            var requestCount = 0
            while (questionCount < 10 && requestCount < 20) {
                var difficulty = getQuestionDifficulty(questionCount)

                val result = async {
                    triviaRemoteDataSource.getQuestion(difficulty, category, token)
                }
                val res: Result<TriviaResponse> = result.await()

                if (res.isSuccess) {
                    res.workAroundFold(onSuccess = {
                        //If response code is 4 then all the questions in that category have been asked & the token needs to be reset.
                        if (it.responseCode == 4) {
                            //TODO - In the future. Handle edge case where questions may get repeated after resetting token
                            try {
                                val resetCall = async { triviaRemoteDataSource.resetToken(token) }
                                resetCall.await()
                            } catch (e: Exception) { }
                        } else {
                            questions.add(it.results.first())
                            questionCount++
                            _triviaStateQuestionProgress.emit(questionCount)
                        }
                    }, onFailure = {})
                }
                requestCount++
            }

            if (questionCount < 10) return@withContext _triviaState.emit(Result.failure(Exception("Could not get questions. Please try again.")))
            else return@withContext _triviaState.emit(Result.success(questions))

        }


    private suspend fun CoroutineScope.resetToken() {

    }

    private fun getQuestionDifficulty(questionCount: Int): Difficulty {
        var difficulty = Difficulty.EASY
        if (questionCount > 4) difficulty =
            if (questionCount < 8) Difficulty.MEDIUM else Difficulty.HARD
        return difficulty
    }

    override suspend fun retrieveToken(): String? {
        withContext(ioDispatcher) {
            val token = async { triviaRemoteDataSource.retrieveToken() }
            token.await().onSuccess()
            {
                if (it.responseCode == 0) _triviaToken.emit(Result.success(it.token))
                else _triviaToken.emit(Result.failure(Exception(it.responseMessage)))
                return@withContext it.token
            }.onFailure {
                _triviaToken.emit(Result.failure(it))
            }
        }
        return null
    }
}