package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.repositories.FakeWouldYouRatherRepository
import com.example.hub_os_device.model.WouldYouRatherResult
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class WouldYouRatherViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: WouldYouRatherViewModel
    private val wouldYouRatherRepository: FakeWouldYouRatherRepository = FakeWouldYouRatherRepository()

    @Before
    fun setup() {
        viewModel = WouldYouRatherViewModel(wouldYouRatherRepository)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get question on success`() = runTest {
        wouldYouRatherRepository.getQuestion()
        Truth.assertThat(collectFlowValue(viewModel.questionsFlow))
            .isInstanceOf(WouldYouRatherResult::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get null on failure`() = runTest {
        wouldYouRatherRepository.setShouldReturnNetworkError(true)
        wouldYouRatherRepository.getQuestion()
        Truth.assertThat(collectFlowValue(viewModel.questionsFlow)).isNull()
    }
}