package com.example.hub_os_device.data.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.hub_os_device.BuildConfig
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(RobolectricTestRunner::class)
@Config(application = Application::class)
@PowerMockIgnore("org.mockito.*", "org.robolectric.*", "android.*")
class FakeSharedPreferencesHelperImpl : SharedPreferencesHelper {

    private val mockContext = mock(Context::class.java)

    private val mockSharedPreferences = mock(SharedPreferences::class.java)

    init {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(
            mockSharedPreferences)
    }

    // Then the SharedPreferences should be returned by the context
    private val sharedPreferences =
        mockContext.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)

    override fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue)
            ?: defaultValue
    }

    override fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

}