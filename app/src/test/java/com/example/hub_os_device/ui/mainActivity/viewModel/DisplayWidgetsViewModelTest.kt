package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.local.FakeSharedPreferencesHelperImpl
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class DisplayWidgetsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: DisplayWidgetsViewModel

    @Before
    fun setup() {
        viewModel = DisplayWidgetsViewModel(FakeSharedPreferencesHelperImpl())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `change to day mode and return false`() = runTest {
        viewModel.changeDayNightMode(false)
        Truth.assertThat(collectFlowValue(viewModel.dayNightModeFlow)).isFalse()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `change to night mode and return true`() = runTest {
        viewModel.changeDayNightMode(true)
        Truth.assertThat(collectFlowValue(viewModel.dayNightModeFlow)).isTrue()
    }
}