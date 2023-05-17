package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentDisplayWidgetsBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.DisplayWidgetsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DisplayWidgetsFragment : Fragment(), ThemeManager.ThemeChangedListener {
    private lateinit var binding: FragmentDisplayWidgetsBinding
    private val BRIGHTNESS = Settings.System.SCREEN_BRIGHTNESS
    private var currentBrightness: Int = 0

    private val displayWidgetsViewModel: DisplayWidgetsViewModel by activityViewModels()

    override fun setInitTheme(theme: Theme) {
        if (this::binding.isInitialized)
            binding.root.background =
                context?.let { ContextCompat.getDrawable(it, theme.mainGradientTheme.drawableGradient) }
    }

    //Brightness Functions
    private fun getSystemBrightness(): Int =
        ((Settings.System.getInt(context?.contentResolver, BRIGHTNESS) * 100) / 255)

    private fun normalize(x: Float, inMax: Float, outMax: Float): Float {
        return (x * outMax) / inMax
    }

    private fun changeBrightness(brightnessLevel: Int): Int {
        return when (brightnessLevel) {
            in 0..24 -> 25
            in 25..49 -> 50
            in 50..74 -> 75
            in 75..90 -> 100
            else -> 25
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.addListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDisplayWidgetsBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                displayWidgetsViewModel.dayNightModeFlow.onEach {
                    if (it != null) binding.lightDarkSwitch.isChecked = it
                }.launchIn(this)
            }
        }

        //Change Day/Night Mode
        binding.lightDarkSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isPressed) displayWidgetsViewModel.changeDayNightMode(b)
        }

        //BRIGHTNESS CONFIGURATION
        currentBrightness = getSystemBrightness()
        binding.brightnessTv.text = getString(R.string.numberPercentage, currentBrightness.toString())

        binding.brightnessButton.setOnClickListener()
        {
            currentBrightness = changeBrightness(brightnessLevel = currentBrightness)
            val brightness: Float = normalize(currentBrightness.toFloat(), 100.0f, 255.0f)
            binding.brightnessTv.text = getString(R.string.numberPercentage, currentBrightness.toString())
            Settings.System.putInt(
                context?.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness.roundToInt()
            )

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitTheme()
    }

    override fun onDestroy() {
        ThemeManager.removeListener(this)
        binding.lightDarkSwitch.setOnCheckedChangeListener(null)
        super.onDestroy()
    }
}