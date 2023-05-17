package com.example.hub_os_device

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioManager
import android.os.Build.ID
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.example.hub_os_device.framework.AdsWorker
import com.example.hub_os_device.ui.RequestScheduler
import com.example.hub_os_device.ui.setupDailyRequests
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HubOSApplication : Application(), Configuration.Provider, DefaultLifecycleObserver, RequestScheduler {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        createNotificationChannels()
        mute()
        this.setupDailyRequests<AdsWorker>(this, "ads", "adsWork")
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun changeBroadcastData(hideNav: Boolean) {
        Intent().also { intent ->
            intent.action = "wangjx.action.broadcast.kidtablet"
            intent.putExtra("flag", hideNav)
            sendBroadcast(intent)
        }
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(ID, "My app", NotificationManager.IMPORTANCE_HIGH)
        channel.description = ""
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        changeBroadcastData(true)
        mute()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        changeBroadcastData(true)
        mute()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        changeBroadcastData(false)
        unMute()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        changeBroadcastData(false)
        unMute()
    }

    private fun mute() {
        try {
            val aManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
        } catch (e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun unMute() {
        try {
            val aManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false)
        } catch (e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}