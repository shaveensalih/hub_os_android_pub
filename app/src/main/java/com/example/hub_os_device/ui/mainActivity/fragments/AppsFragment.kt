package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.contains
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.Transition
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentAppsBinding
import com.example.hub_os_device.databinding.MenuWidgetContentBinding
import com.example.hub_os_device.databinding.TableNumWidgetContentBinding
import com.example.hub_os_device.ui.mainActivity.fragments.games.BackspaceAppFragment
import com.example.hub_os_device.ui.mainActivity.fragments.games.GamesGridApp
import com.example.hub_os_device.ui.mainActivity.viewModel.EstablishmentViewModel
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppsFragment : Fragment(), ThemeManager.ThemeChangedListener {
    private lateinit var binding: FragmentAppsBinding
    private val establishmentViewModel: EstablishmentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.addListener(this)
        requireActivity().supportFragmentManager.setFragmentResultListener("openApp",
            this) { requestKey, bundle ->
            val cardId: Int = bundle.getInt("cardId")
            openApp(cardId)
        }
    }

    override fun onDestroy() {
        ThemeManager.removeListener(this)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnPreDraw { startPostponedEnterTransition() }

        val decorView: View = requireActivity().window.decorView
        val windowBackground = decorView.background

        val weatherBundle = Bundle()
        weatherBundle.putBoolean(getString(R.string.showWeekWeather), true)
        binding.largeWeatherContainer.getFragment<WeatherWidgetFragment>().arguments = weatherBundle

        binding.tableNumStub.setOnInflateListener { _, inflateId ->
            val stubBinding = TableNumWidgetContentBinding.bind(inflateId)
            stubBinding.tableNumTv.text =
                activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)
                    ?.getString(getString(R.string.table_num_shared_preferences), "NO")
        }

        binding.menuStub.setOnInflateListener { _, inflateId ->
            val qrCodeBundle = Bundle()
            qrCodeBundle.putBoolean(getString(R.string.showWifiCode), true)
            val stubBinding = MenuWidgetContentBinding.bind(inflateId)
            stubBinding.qrCodeFragment.getFragment<QrCodeFragment>().arguments = qrCodeBundle
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                establishmentViewModel.dineOSIntegrationFlow.collectLatest {
                    binding.selfServiceIcon.visibility = if (it) View.VISIBLE else View.GONE
                    binding.splitTheBillAppIcon.visibility = if (it) View.VISIBLE else View.GONE
                    binding.menuAppIcon.isEnabled = it
                }
            }
        }

        inflateWidgetStubs()

        binding.appsGridLayout.forEach { card ->
            if (card is CardView && card.children.first() is BlurView)
                (card.children.first() as BlurView).setupWith(
                    requireActivity().findViewById(android.R.id.content),
                    RenderScriptBlur(requireContext()))
                    .setFrameClearDrawable(windowBackground) // Optional
                    .setBlurRadius(20f)

            card.setOnClickListener {
                val newScreen: Fragment? = getScreen(card)

                if (newScreen != null) {
                    logAppSelection(card.tag as String)
                    changeScreen(newScreen, card)
                }
            }
        }
    }

    private fun changeScreen(
        newScreen: Fragment?,
        card: View,
    ) {
        newScreen?.sharedElementReturnTransition = setupMaterialContainerTransform(false)
        newScreen?.sharedElementEnterTransition = setupMaterialContainerTransform(true)

        parentFragmentManager
            .beginTransaction()
            .addSharedElement(card, card.transitionName)
            .addToBackStack("initial")
            .replace(R.id.app_view_container, newScreen!!)
            .setReorderingAllowed(true)
            .commit()
    }

    private fun openApp(appId: Int) {
        val card: View = binding.root.findViewById(appId)
        val screen = getScreen(binding.root.findViewById(appId))
        changeScreen(screen, card)
    }

    private fun getScreen(card: View): Fragment? {
        when (card.id) {
            //TODO - Make sure card has transition name
            R.id.hub_app_icon -> {
                return SelectedAppContainerFragment.newInstance(card.transitionName,
                    HubAppLockScreenFragment(),
                    R.drawable.hub_app_background_gradient,
                    R.drawable.hub_app_background_gradient_night)
            }
            R.id.menu_app_icon -> {
                return SelectedAppContainerFragment.newInstance(card.transitionName, MenuFragment(),
                    R.drawable.hub_app_background_gradient,
                    R.drawable.hub_app_background_gradient_night)
            }
            R.id.split_the_bill_app_icon -> {
                return SelectedAppContainerFragment.newInstance(card.transitionName,
                    SplitTheBillFragment(),
                    R.drawable.hub_app_background_gradient,
                    R.drawable.hub_app_background_gradient_night)
            }
            R.id.games_icon -> {
                return SelectedAppContainerFragment.newInstance(card.transitionName, GamesGridApp(),
                    R.drawable.games_bg_gradient, R.drawable.games_bg_gradient)
            }
            R.id.backspace_icon -> {
                return SelectedAppContainerFragment.newInstance(card.transitionName,
                    BackspaceAppFragment(),
                    R.drawable.backspace_app_bg,
                    R.drawable.backspace_app_bg)
            }
            R.id.topic_of_day_app_icon -> {
                val topicOfTheDayDialogFragment = TopicOfTheDayDialogFragment()
                activity?.supportFragmentManager?.let { fragMan ->
                    topicOfTheDayDialogFragment.show(
                        fragMan,
                        "topic_dialog"
                    )
                }
                return null
            }
        }
        return null
    }

    private fun inflateWidgetStubs() {
        binding.tableNumStub.inflate()
        binding.topicsStub.inflate()
        binding.gamesStub.inflate()
        binding.menuStub.inflate()
        binding.clockStub.inflate()
        binding.surveyStub.inflate()
        binding.displayStub.inflate()
        binding.splitBillStub.inflate()
        binding.selfServiceStub.inflate()
        binding.stub.inflate()
        setInitTheme()
    }


    private fun logAppSelection(appTag: String) {
        Firebase.analytics.logEvent("app_tapped", bundleOf("app_tag" to appTag))
    }

    private fun setupMaterialContainerTransform(isEntering: Boolean): Transition {
        return MaterialContainerTransform().apply {
            interpolator = AccelerateInterpolator()
            duration = 300
            allowEnterTransitionOverlap = true
        }.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionEnd(transition: Transition) {
                if (isEntering) {
                    setFragmentResult("requestKey", bundleOf("makeSmall" to true))
                }
            }

            override fun onTransitionCancel(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAppsBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AppsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun setInitTheme(theme: Theme) {
        binding.appsGridLayout.forEach {
            if (binding.appsGridLayout.contains(it)) {
                val card = if (it is CardView) it else null
                val cardChild = card?.children?.first()
                if (cardChild?.tag != "no_dark_mode")
                    card?.children?.first()?.background = ContextCompat.getDrawable(requireContext(),
                        theme.mainGradientTheme.drawableGradient
                    )
            }
        }
    }
}