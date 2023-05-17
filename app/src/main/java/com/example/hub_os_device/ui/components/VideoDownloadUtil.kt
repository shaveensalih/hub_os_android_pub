package com.example.hub_os_device.ui.components

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File


class VideoDownloadUtil(context: Context) {

    object CacheFolder {

        val dataSourceFactory = DefaultHttpDataSource.Factory()
        private val downloadExecutor = Runnable::run
        var sDownloadCache: SimpleCache? = null
        private var databaseProvider: StandaloneDatabaseProvider? = null
        var downloadManager: DownloadManager? = null

        fun getSimpleCacheInstance(context: Context): SimpleCache {
            getDatabaseInstance(context)
            if (sDownloadCache == null) sDownloadCache = SimpleCache(
                File(context.cacheDir, "exoCache"),
                NoOpCacheEvictor(),
                databaseProvider!!

            )
            return sDownloadCache!!
        }

        private fun getDatabaseInstance(context: Context): StandaloneDatabaseProvider {
            if (databaseProvider == null) {
                databaseProvider = StandaloneDatabaseProvider(context)
            }

            return databaseProvider!!
        }

        fun getDownloadManager(context: Context): DownloadManager {

            if (downloadManager == null) {
                downloadManager = DownloadManager(
                    context,
                    getDatabaseInstance(context),
                    getSimpleCacheInstance(context),
                    dataSourceFactory,
                    downloadExecutor
                )
                downloadManager!!.maxParallelDownloads = 5
            }

            return downloadManager!!
        }

    }

    var cacheDataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
        .setCache(CacheFolder.getSimpleCacheInstance(context))
        .setUpstreamDataSourceFactory(CacheFolder.dataSourceFactory)
        .setCacheWriteDataSinkFactory(null)


    val player: ExoPlayer = ExoPlayer.Builder(context).setMediaSourceFactory(
        DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)
    ).build()

    init {


        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(
                C.TRACK_TYPE_AUDIO, true
            )
            .build()
    }
}
