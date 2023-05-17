package com.example.hub_os_device.ui.splashActivity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.local.FakeSharedPreferencesHelperImpl
import com.example.hub_os_device.data.repositories.FakeConfigRepository
import com.example.hub_os_device.data.repositories.FakeDeviceInfoRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class SplashViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SplashViewModel
    private lateinit var deviceInfoRepository: FakeDeviceInfoRepository
    private lateinit var configRepository: FakeConfigRepository

    @Before
    fun setup() {
        configRepository = FakeConfigRepository()
        deviceInfoRepository = FakeDeviceInfoRepository()
        viewModel = SplashViewModel(configRepository, deviceInfoRepository, FakeSharedPreferencesHelperImpl())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `return device not registered error if 404`() =
        runTest {
            deviceInfoRepository.setHttpExceptionCode(404)
            viewModel.makeInitialCalls()
            assertThat(collectFlowValue(viewModel.config)).isInstanceOf(ConfigState.DEVICENOTREGISTERED::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `return update needed when new version is out`() =
        runTest {
            configRepository.setNewVersion("1.0.1")
            viewModel.makeInitialCalls()
            assertThat(collectFlowValue(viewModel.config)).isInstanceOf(ConfigState.UPDATENEEDED::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `return network error from config repository`() =
        runTest {
            configRepository.setShouldReturnNetworkError(true)
            viewModel.makeInitialCalls()
            assertThat(collectFlowValue(viewModel.config)).isEqualTo(ConfigState.FAILURE("Error"))
            configRepository.setShouldReturnNetworkError(false)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `return network error from device repository`() =
        runTest {
            deviceInfoRepository.setShouldReturnNetworkError(true)
            viewModel.makeInitialCalls()
            assertThat(collectFlowValue(viewModel.config)).isEqualTo(ConfigState.FAILURE("Network Exception"))
            deviceInfoRepository.setShouldReturnNetworkError(false)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `register device successfully`() =
        runTest {
            viewModel.registerDevice("000000")
            assertThat(collectFlowValue(viewModel.config)).isInstanceOf(ConfigState.SUCCESS::class.java)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `register device returns network error`() =
        runTest {
            deviceInfoRepository.setShouldReturnNetworkError(true)
            viewModel.registerDevice("000000")
            assertThat(collectFlowValue(viewModel.registerState)).isEqualTo(RegisterState.FAILURE("Network Exception"))
            deviceInfoRepository.setShouldReturnNetworkError(false)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `pin code is incorrect when registering device`() =
        runTest {
            viewModel.registerDevice("123456")
            assertThat(collectFlowValue(viewModel.registerState)).isInstanceOf(RegisterState.INCORRECTPIN::class.java)
        }
}