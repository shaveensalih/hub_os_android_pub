package com.example.hub_os_device.ui.mainActivity.fragments

import AdvertisementRecyclerViewAdapter
import ImageSize
import Theme
import ThemeManager
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentLargeAdPageBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.AdsViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LargeAdPage : Fragment(), ThemeManager.ThemeChangedListener {
    private lateinit var binding: FragmentLargeAdPageBinding
    private val adsViewModel: AdsViewModel by activityViewModels()

    var lastStoredRecyclerViewPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.addListener(this)
    }

    override fun onDestroy() {
        ThemeManager.removeListener(this)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLargeAdPageBinding.inflate(layoutInflater)
        val view = binding.root
        binding.selfServiceStub.inflate()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selfServiceIcon.setOnClickListener {
            Firebase.analytics.logEvent("app_tapped", bundleOf("app_tag" to it.tag))
        }

        setInitTheme()

        setupRecyclerView()

        binding.goToAppsButton.setOnClickListener()
        {
            setFragmentResult("changePage", bundleOf("page" to 1))
        }

        binding.qrCodeFragment.setOnClickListener()
        {
            Firebase.analytics.logEvent("app_tapped", bundleOf("app_tag" to "Menu App"))
            //TODO - When another app is open it does not close that app and open the menu app
            setFragmentResult("changePage", bundleOf("page" to 1))
            setFragmentResult("openApp", bundleOf("cardId" to R.id.menu_app_icon))
        }

        val decorView: View = requireActivity().window.decorView
        val windowBackground = decorView.background

        binding.gridWidgets.forEach {
            if ((it is CardView) && it.children.first() is BlurView)
                (it.children.first() as BlurView).setupWith(
                    requireActivity().findViewById(android.R.id.content),
                    RenderScriptBlur(requireContext()))
                    .setFrameClearDrawable(windowBackground) // Optional
                    .setBlurRadius(20f)
        }

        lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                adsViewModel.currentAdIndexState.onEach { adIndex ->
                    if (shouldJumpToNewPosition(adIndex)) {
                        binding.largeAdRecyclerView.scrollToPosition(adIndex)
                    } else {
                        binding.largeAdRecyclerView.smoothScrollToPosition(adIndex)
                    }
                    lastStoredRecyclerViewPos = adIndex
                }.launchIn(this)
            }
            adsViewModel.dineOSIntegrationFlow.onEach {
                binding.qrCodeFragment.isEnabled = it
                binding.selfServiceIcon.visibility = if (it) View.VISIBLE else View.GONE
                val layoutParams: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.weight = if (it) (binding.serviceLl.weightSum - 1) else binding.serviceLl.weightSum
                layoutParams.gravity = Gravity.CENTER
                binding.broadcastMessageFragment.layoutParams = layoutParams

            }.launchIn(this)
        }
    }

    private fun shouldJumpToNewPosition(adIndex: Int) =
        (adIndex - lastStoredRecyclerViewPos) > 1 || (adIndex - lastStoredRecyclerViewPos) < 0

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED)
            {
                adsViewModel.adsState.collectLatest {
                    val adapter =
                        AdvertisementRecyclerViewAdapter(
                            requireActivity().applicationContext,
                            ImageSize.LARGE,
                            it.ads
                        )
                    binding.largeAdRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.largeAdRecyclerView.adapter = adapter
                    binding.largeAdRecyclerView.setAdvertisements(it)
                }
            }
        }
        binding.largeAdRecyclerView.addOnItemTouchListener(object :
            RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }
        })
        if (binding.largeAdRecyclerView.onFlingListener == null) {
            val helper = PagerSnapHelper()
            helper.attachToRecyclerView(binding.largeAdRecyclerView)
        }
    }

    override fun setInitTheme(theme: Theme) {
        binding.weatherWidgetFragment.background =
            context?.let { ContextCompat.getDrawable(it, theme.mainGradientTheme.drawableGradient) }
        binding.qrCodeFragment.background =
            context?.let { ContextCompat.getDrawable(it, theme.mainGradientTheme.drawableGradient) }
//        binding.root.background =
//            context?.let { ContextCompat.getDrawable(it, theme.mainGradientTheme.drawableGradient) }
    }
}