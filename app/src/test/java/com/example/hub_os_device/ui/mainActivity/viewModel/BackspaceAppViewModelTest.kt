package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.repositories.FakeConfigRepository
import com.example.hub_os_device.model.Socials
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class BackspaceAppViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: BackspaceAppViewModel
    private lateinit var configRepository: FakeConfigRepository

    @Before
    fun setup() {
        configRepository = FakeConfigRepository()
        viewModel = BackspaceAppViewModel(configRepository)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get social media links with successful request`() =
        runTest {
            configRepository.getConfig()
            Truth.assertThat(collectFlowValue(viewModel.linksFlow)).isInstanceOf(Socials::class.java)
        }
}