package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.repositories.FakeDeviceInfoRepository
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class BroadcastMessageFragmentViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: BroadcastMessageFragmentViewModel
    private lateinit var deviceInfoRepository: FakeDeviceInfoRepository

    @Before
    fun setup() {
        deviceInfoRepository = FakeDeviceInfoRepository()
        viewModel = BroadcastMessageFragmentViewModel(deviceInfoRepository)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get broadcast message with successful request`() =
        runTest {
            deviceInfoRepository.getDeviceInfo(0)
            Truth.assertThat(collectFlowValue(viewModel.broadcastMessageFlow))
                .isInstanceOf(String::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `get null broadcast message on error`() =
        runTest {
            deviceInfoRepository.setShouldReturnNetworkError(true)
            deviceInfoRepository.getDeviceInfo(0)
            Truth.assertThat(collectFlowValue(viewModel.broadcastMessageFlow)).isNull()
        }
}