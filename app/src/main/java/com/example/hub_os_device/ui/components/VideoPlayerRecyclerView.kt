package com.example.hub_os_device.ui.components

import AdvertisementRecyclerViewAdapter
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hub_os_device.ui.mainActivity.viewModel.AdvertisementListUIState
import com.example.hub_os_device.ui.mainActivity.viewModel.AdvertisementUIState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView

class VideoPlayerRecyclerView : RecyclerView {
    // ui
    private var thumbnail: ImageView? = null
    private var viewHolderParent: View? = null
    private var frameLayout: FrameLayout? = null
    private var videoSurfaceView: StyledPlayerView? = null
    private var videoPlayer: ExoPlayer? = null
    private lateinit var videoDownloadUtil: VideoDownloadUtil

    // vars
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var classContext: Context? = null
    private var playPosition = -1
    private var isVideoViewAdded = false
    private var advertisements: AdvertisementListUIState? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        classContext = context.applicationContext
        videoDownloadUtil = VideoDownloadUtil(classContext!!)
        VideoDownloadUtil.CacheFolder.getSimpleCacheInstance(classContext!!)
        VideoDownloadUtil.CacheFolder.getDownloadManager(classContext!!)
        val display: Display =
            (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y

//        //Player view init
        videoSurfaceView = StyledPlayerView(context)
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        videoSurfaceView!!.setKeepContentOnPlayerReset(true)

        // 2. Create the player
        videoPlayer = videoDownloadUtil.player
        // Bind the player to the view.

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    thumbnail?.visibility = VISIBLE
                    playVideo()
                }
            }
        })

        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent!! == view) {
                    resetVideoView()
                }
            }
        })

        videoPlayer!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                    }
                    Player.STATE_ENDED -> {
                        videoPlayer!!.seekTo(0)
                    }
                    Player.STATE_IDLE -> {}
                    Player.STATE_READY -> {
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                    else -> {}
                }
            }
        })
    }

    fun playVideo() {
        val targetPosition: Int =
            findPlayPosition() ?: return

        //Finding view holder of current position
        val currentPosition: Int =
            targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        println(currentPosition)
        val child: View = getChildAt(currentPosition) ?: return
        println("child $child")
        val holder: AdvertisementRecyclerViewAdapter.VideoAdViewHolder =
            if (child.tag is AdvertisementRecyclerViewAdapter.VideoAdViewHolder) (child.tag as AdvertisementRecyclerViewAdapter.VideoAdViewHolder) else return

        //binding video surface view
        videoSurfaceView!!.useController = false
        videoSurfaceView!!.player = videoPlayer

        // remove any old surface views from previously playing videos
        videoSurfaceView?.visibility = INVISIBLE
        if (videoSurfaceView?.parent != null) removeVideoView(videoSurfaceView)

        thumbnail = holder.itemBinding.thumbnail
//        viewHolderParent = holder.itemView
//        requestManager = holder.requestManager
        frameLayout = holder.itemBinding.mediaContainer

        val advertisement: AdvertisementUIState? =
            advertisements?.ads?.get(targetPosition % advertisements!!.ads.size)

        val currentMediaItem = advertisement?.downloadRequest?.toMediaItem()
        val mediaSource: MediaSource? = currentMediaItem?.let {
            ProgressiveMediaSource.Factory(videoDownloadUtil.cacheDataSourceFactory)
                .createMediaSource(it)
        }

        if (currentMediaItem != null) {
            videoPlayer!!.setMediaSource(mediaSource!!)
            videoPlayer!!.prepare()
            videoPlayer!!.playWhenReady = true
        }
    }

    private fun findPlayPosition(): Int? {
        val targetPosition: Int
        val startPosition: Int =
            (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        var endPosition: Int = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        // if there is more than 2 list-items on the screen, set the difference to be 1
        if (endPosition - startPosition > 1) {
            endPosition = startPosition + 1
        }

        // something is wrong. return.
        if (startPosition < 0 || endPosition < 0) {
            return null
        }

        // if there is more than 1 list-item on the screen
        targetPosition = if (startPosition != endPosition) {
            val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
            val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)
            if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
        } else {
            startPosition
        }

        // video is already playing so return
        if (targetPosition == playPosition) {
            return null
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition

        return targetPosition
    }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at: Int =
            playPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        println("getVisibleVideoSurfaceHeight: at: $at")
        val child: View = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    // Remove the old player
    private fun removeVideoView(videoView: StyledPlayerView?) {
        val parent = videoView?.parent as ViewGroup
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
        }
    }

    private fun addVideoView() {
        frameLayout!!.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView!!.requestFocus()
        videoSurfaceView!!.visibility = VISIBLE
        videoSurfaceView!!.alpha = 1f
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView!!.visibility = INVISIBLE
            thumbnail?.visibility = VISIBLE
        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer!!.release()
            videoPlayer = null
        }
        viewHolderParent = null
    }

    fun setAdvertisements(ads: AdvertisementListUIState) {
        val downloadedVids = mutableMapOf<Int, Int>()
        for (ad in ads.ads) {
            if (ad.downloadRequest != null) {
                if(downloadedVids.containsKey(ad.id)) continue
                DownloadService.sendAddDownload(
                    classContext!!,
                    VideoDownloadService::class.java,
                    ad.downloadRequest!!,
                    false
                )
                downloadedVids[ad.id] = ad.id
            }
        }

        advertisements = ads
    }

    companion object {
        private const val TAG = "VideoPlayerRecyclerView"
    }
}