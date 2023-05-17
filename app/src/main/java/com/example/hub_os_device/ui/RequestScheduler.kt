package com.example.hub_os_device.ui

import android.app.Application
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

interface RequestScheduler {
    fun millisToNextHour(calendar: Calendar): Long {
        val minutes: Int = calendar.get(Calendar.MINUTE)
        val seconds: Int = calendar.get(Calendar.SECOND)
        val millis: Int = calendar.get(Calendar.MILLISECOND)
        val minutesToNextHour: Int = 60 - minutes
        val secondsToNextHour: Int = 60 - seconds
        val millisToNextHour: Int = 1000 - millis
        return (minutesToNextHour * 60 * 1000 + secondsToNextHour * 1000 + millisToNextHour).toLong()
    }

    //Setup periodic requests for 12:01AM each day by getting the delay needed for the initial call
    fun millisToNextDay(): Long {
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

inline fun <reified T : ListenableWorker> RequestScheduler.setupDailyRequests(
    application: Application, tag: String, uniqueWorkName: String,
) {
    val constraints: Constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val worker = PeriodicWorkRequestBuilder<T>(
        1, TimeUnit.DAYS,
        5, TimeUnit.MINUTES).setConstraints(constraints)
        .setInitialDelay(millisToNextDay(), TimeUnit.MILLISECONDS)
        .addTag(tag).build()
    val workMan = WorkManager.getInstance(application.applicationContext)
    workMan.enqueueUniquePeriodicWork(
        uniqueWorkName,
        ExistingPeriodicWorkPolicy.REPLACE,
        worker
    )

//        val listener: ListenableFuture<List<WorkInfo>> = workMan.getWorkInfosByTag("weather")
//        workMan.getWorkInfoByIdLiveData(weatherWorker.id).observeForever {
//            println("Info: ${it.state}, ${it.progress}, ${it.runAttemptCount}, ${it.outputData}")
//        }
}