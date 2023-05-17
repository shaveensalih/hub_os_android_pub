package com.example.hub_os_device.ui.components

import android.app.Notification
import android.content.Context
import android.os.Build.ID
import com.example.hub_os_device.R
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper

class VideoDownloadService : DownloadService(1, DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL) {

    private lateinit var notificationHelper: DownloadNotificationHelper
    private lateinit var context: Context

    override fun onCreate() {
        super.onCreate()
        context = this
        notificationHelper = DownloadNotificationHelper(this, ID)
    }

    public override fun getDownloadManager(): DownloadManager {
        return VideoDownloadUtil.CacheFolder.getDownloadManager(this)
    }

    override fun getScheduler(): Scheduler? {
        return null
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return notificationHelper.buildProgressNotification(
            context,
            R.drawable.hub_icon,
            null,
            "",
            downloads
        )
    }
}