package com.example.hub_os_device.ui.mainActivity.fragments

import AdvertisementRecyclerViewAdapter
import ImageSize
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.hub_os_device.databinding.FragmentSmallAdPageBinding
import com.example.hub_os_device.ui.LooperInterface
import com.example.hub_os_device.ui.mainActivity.viewModel.AdsViewModel
import com.example.hub_os_device.ui.mainActivity.viewModel.AdvertisementUIState
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SmallAdPage : Fragment(), LooperInterface {
    private var lastStoredRecyclerViewPos: Int = 0
    private lateinit var binding: FragmentSmallAdPageBinding
    private val adsViewModel: AdsViewModel by activityViewModels()
    private var logAdEvents: Boolean = false

    override var mHandler: Handler? = Handler(Looper.getMainLooper())
    override var mRunnable: Runnable? = null
    override var mTime: Long? = 60000

    var advertiserLogMap = mutableMapOf<String, Int>()

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            requireActivity().supportFragmentManager.setFragmentResultListener("resetScheduler",
                this) { requestKey, bundle ->
                restartScheduler()
            }
            logAdEvents = true
            startHandler()
        } else {
            requireActivity().supportFragmentManager.clearFragmentResultListener("resetScheduler")
            logAdEvents = false
            advertiserLogMap.forEach {
                Firebase.analytics.logEvent("active_ad_count") {
                    bundleOf("advertiser" to it.key, "ad_count_during_session" to it.value)
                }
            }
            stopHandler()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRunnable = Runnable {
            if (activity != null && isAdded)
                requireActivity().supportFragmentManager.setFragmentResult("changePage",
                    bundleOf("page" to 0))
        }

        binding.root.setOnInterceptEvent {
            stopHandler()
            startHandler()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSmallAdPageBinding.inflate(layoutInflater)
        val view = binding.root

        childFragmentManager.setFragmentResultListener(
            "requestKey",
            viewLifecycleOwner
        ) { _, bundle ->
            val makeSmall: Boolean = bundle.getBoolean("makeSmall")
            setupRecyclerView(adsViewModel.adsState.value.ads,
                if (makeSmall) ImageSize.SMALL else ImageSize.MEDIUM)
            if (makeSmall) binding.root.transitionToEnd() else binding.root.transitionToStart()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                adsViewModel.adsState.collectLatest {
                    setupRecyclerView(it.ads, ImageSize.MEDIUM)
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adsViewModel.currentAdIndexState.collect { adIndex ->
                    if (shouldJumpToNewPosition(adIndex)) {
                        binding.smallAdRecyclerView.scrollToPosition(adIndex)
                    } else {
                        binding.smallAdRecyclerView.smoothScrollToPosition(adIndex)
                    }
                    if (logAdEvents) {
                        val advertiserName = adsViewModel.getCurrentAd(adIndex)?.advertiser?.name
                        if (advertiserName != null) advertiserLogMap[advertiserName] =
                            (advertiserLogMap[advertiserName] ?: 0) + 1
                    }
                    lastStoredRecyclerViewPos = adIndex
                }
            }
        }

        return view
    }

    private fun shouldJumpToNewPosition(adIndex: Int) =
        (adIndex - lastStoredRecyclerViewPos) > 2 || (adIndex - lastStoredRecyclerViewPos) < 0

    private fun setupRecyclerView(ads: List<AdvertisementUIState>, imageSize: ImageSize) {

        val adapter = AdvertisementRecyclerViewAdapter(requireActivity().applicationContext, imageSize, ads)
        binding.smallAdRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.smallAdRecyclerView.adapter = adapter
        binding.smallAdRecyclerView.addOnItemTouchListener(object :
            RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }
        })
        if (binding.smallAdRecyclerView.onFlingListener == null) {
            val helper = PagerSnapHelper()
            helper.attachToRecyclerView(binding.smallAdRecyclerView)
        }
        binding.smallAdRecyclerView.scrollToPosition(lastStoredRecyclerViewPos)
    }

    private fun restartScheduler() {
        stopHandler()
        startHandler()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SmallAdPage().apply {
                arguments = Bundle().apply {

                }
            }
    }
}