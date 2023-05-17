package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.TriviaRepository
import com.example.hub_os_device.model.Difficulty
import com.example.hub_os_device.model.TriviaCategory
import com.example.hub_os_device.model.TriviaQuestion
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class TriviaViewModel @Inject constructor(private val triviaRepository: TriviaRepository) : ViewModel() {

    init {
        setUpTriviaFlowListener()
        getToken()
    }

    private fun setUpTriviaFlowListener() {
        viewModelScope.launch {
            triviaRepository.triviaState.collectLatest {
                _questionsFlow.emit(it)
            }
        }
    }

    private var tokenFlow: StateFlow<String?> =
        triviaRepository.triviaToken.map {
            it?.workAroundFold(onSuccess = { token -> return@map token }, onFailure = {})
            return@map null
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var _score: Int = 0
    var score: Int = 0
        get() {
            return _score
        }

    private val _questionsFlow =
        MutableSharedFlow<Result<List<TriviaQuestion>>?>(1)
    val questionsFlow: SharedFlow<TriviaQuestionsState?> = _questionsFlow.map {
        it?.workAroundFold(onSuccess = { questions ->
            questions.sortedBy { question ->
                when (question.difficulty) {
                    Difficulty.EASY -> 1
                    Difficulty.MEDIUM -> 2
                    Difficulty.HARD -> 3
                }
            }
            return@map TriviaQuestionsState.SUCCESS(questions)
        }, onFailure = { error ->
            return@map TriviaQuestionsState.FAILURE(error.message ?: "")
        })
        return@map null
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val questionProgressFlow: SharedFlow<Int> = triviaRepository.triviaStateQuestionProgress

    fun getQuest(category: TriviaCategory) {
        viewModelScope.launch {
            println(tokenFlow.value)
            triviaRepository.getQuestions(category, tokenFlow.value)
        }
    }

    private fun getToken() {
        viewModelScope.launch {
            triviaRepository.retrieveToken()
        }
    }

    fun incrementScore() {
        _score++
    }

    fun resetTrivia() {
        _score = 0
        viewModelScope.launch {
            _questionsFlow.emit(null)
        }
    }
}

sealed class TriviaQuestionsState {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    object LOADING : TriviaQuestionsState()
    data class SUCCESS(val questions: List<TriviaQuestion>?) : TriviaQuestionsState()
    data class FAILURE(val message: String) : TriviaQuestionsState()
}