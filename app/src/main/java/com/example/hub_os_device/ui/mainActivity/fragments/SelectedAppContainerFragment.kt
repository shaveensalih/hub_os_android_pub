package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.graphics.Rect
import android.os.*
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentSelectedAppContainerBinding
import com.example.hub_os_device.ui.LooperInterface

private const val APP = "app"
private const val BG_DAY = "bg_day"
private const val BG_NIGHT = "bg_night"
private const val TRANSITION_NAME = "transition_name"

class SelectedAppContainerFragment(
) : Fragment(), ThemeManager.ThemeChangedListener, LooperInterface {
    private lateinit var binding: FragmentSelectedAppContainerBinding
    private var app: Parcelable? = null
    private var bgDay: Int? = null
    private var bgNight: Int? = null
    private var transitionName: String? = null

    override var mHandler: Handler? = Handler(Looper.getMainLooper())
    override var mRunnable: Runnable? = null
    override var mTime: Long? = 120000

    private var inactivityToast: Toast? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var countdownNum = 10
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(p0: Long) {
                if (activity != null && isAdded)
                    requireActivity().runOnUiThread {
                        countdownNum -= 1
                        assignToast(countdownNum)
                        inactivityToast?.show()
                    }
            }

            override fun onFinish() {
                if (activity != null && isAdded)
                    requireActivity().runOnUiThread {
                        closeApp()
                    }
            }
        }
        mRunnable =
            Runnable {
                countdownNum = 10
                assignToast(countdownNum)
                inactivityToast?.show()
                countDownTimer?.start()
            }

        startHandler()
    }

    private fun assignToast(countdownNum: Int) {
        inactivityToast = Toast.makeText(requireContext(),
            "The currently opened app will close due to inactivity in $countdownNum seconds",
            Toast.LENGTH_SHORT)
    }

    override fun stopHandler() {
        countDownTimer?.cancel()
        inactivityToast?.cancel()
        super.stopHandler()
    }

    override fun onPause() {
        stopHandler()
        super.onPause()
    }

    override fun onResume() {
        startHandler()
        super.onResume()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            app = it.getParcelable(APP)
            bgDay = it.getInt(BG_DAY)
            bgNight = it.getInt(BG_NIGHT)
            transitionName = it.getString(TRANSITION_NAME)
        }

        view.transitionName = transitionName

        onThemeChanged(ThemeManager.theme)

        if (app != null) {
            parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in_long, R.anim.fade_out)
                .replace(R.id.app_content_container, app as Fragment)
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit()
        }
    }

    override fun onDestroy() {
        ThemeManager.removeListener(this)
        stopHandler()
        super.onDestroy()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentSelectedAppContainerBinding.inflate(layoutInflater)

        binding.constraintLayoutContainer.setOnInterceptEvent {
            stopHandler()
            startHandler()
        }

        ThemeManager.addListener(this)

        val parent =
            binding.returnToAppsButton.parent as View

        parent.post {
            val rect = Rect()
            binding.returnToAppsButton.getHitRect(rect)
            rect.top -= 50
            rect.left -= 50
            rect.bottom += 50
            rect.right += 50
            parent.touchDelegate = TouchDelegate(rect, binding.returnToAppsButton)
        }

        binding.returnToAppsButton.setOnClickListener()
        {
            closeApp()
        }

        val view = binding.root
        return view
    }

    private fun closeApp() {
        parentFragmentManager.popBackStack("initial", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        postponeEnterTransition()
        setFragmentResult("requestKey", bundleOf("makeSmall" to false))
    }

    override fun onThemeChanged(theme: Theme) {
        super.onThemeChanged(theme)

        when (theme) {
            Theme.LIGHT -> binding.constraintLayoutContainer.background =
                ContextCompat.getDrawable(binding.root.context, bgDay!!)
            Theme.DARK -> if (bgNight != null) binding.constraintLayoutContainer.background =
                ContextCompat.getDrawable(binding.root.context, bgNight!!)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(transitionName: String, app: Parcelable, bgDay: Int, bgNight: Int?) =
            SelectedAppContainerFragment().apply {
                arguments = Bundle().apply {
                    putString(TRANSITION_NAME, transitionName)
                    putParcelable(APP, app)
                    putInt(BG_DAY, bgDay)
                    if (bgNight != null) {
                        putInt(BG_NIGHT, bgNight)
                    }
                }
            }
    }
}