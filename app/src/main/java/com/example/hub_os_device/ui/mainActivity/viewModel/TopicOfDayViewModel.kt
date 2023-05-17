package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.TopicOfDayRepository
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TopicOfDayViewModel @Inject constructor(
    private val topicOfDayRepository: TopicOfDayRepository,
) :
    ViewModel() {

    init {
        getTopics()
        initTopicOfDayDailyCall()
    }

    val topicOfDayFlow: StateFlow<String?> =
        topicOfDayRepository.topicOfDayState.map {
            it.workAroundFold(onSuccess = { topicOfDay ->
                return@map topicOfDay[0]
            }, onFailure = {
                return@map listOf("If you could live in any country, where would you live?",
                    "Who is your role model and why?",
                    "What would you do if you won a million dollars?",
                    "What is more important to you, money, love or happiness",
                    "If you could have any superpower, what would it be?",
                    "If you could be anyone for a day, who would you be?"
                ).random()
            })
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun initTopicOfDayDailyCall() {
        val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

        scheduler.scheduleAtFixedRate(
            {
                getTopics()
            },
            millisToNextDay(),
            60 * 60 * 24 * 1000,
            TimeUnit.MILLISECONDS
        )
    }

    private fun getTopics() {
        viewModelScope.launch {
            topicOfDayRepository.getTopicOfDay()
        }
    }

    private fun millisToNextDay(): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        dueDate.set(Calendar.HOUR_OF_DAY, 0)
        dueDate.set(Calendar.MINUTE, 1)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        return dueDate.timeInMillis - currentDate.timeInMillis
    }
}

