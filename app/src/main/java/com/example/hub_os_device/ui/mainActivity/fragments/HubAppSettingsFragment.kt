package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.BuildConfig
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentHubAppSettingsBinding
import com.example.hub_os_device.ui.components.ActionButtonType
import com.example.hub_os_device.ui.components.NumberPadView
import com.example.hub_os_device.ui.mainActivity.viewModel.EstablishmentUIState
import com.example.hub_os_device.ui.mainActivity.viewModel.EstablishmentViewModel
import com.example.hub_os_device.ui.models.UIResult
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.launch

class HubAppSettingsFragment : Fragment(), ThemeManager.ThemeChangedListener {

    lateinit var binding: FragmentHubAppSettingsBinding
    private val estViewModel: EstablishmentViewModel by activityViewModels()
    private var tableCount = 65

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
        // Inflate the layout for this fragment
        binding = FragmentHubAppSettingsBinding.inflate(layoutInflater)
        setInitTheme()

        fun setDebugButtonText() {
            binding.debugButtonTv.text =
                if (activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)
                        ?.getBoolean(
                            getString(R.string.debug_mode_shared_preferences),
                            false
                        ) == true
                ) "Debug: ON" else "Debug: OFF"
        }

        fun toggleLoadingMode(isLoading: Boolean) {
            binding.reloadButtonTv.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            binding.reloadButtonProgressBar.visibility =
                if (isLoading) View.VISIBLE else View.INVISIBLE
        }

        val sharedPref = activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)

        binding.numberPad.setActionButtonType(ActionButtonType.Enter)

        binding.deviceIdTv.text = getString(R.string.device_id_display,
            requireActivity().applicationContext.getSharedPreferences("deviceIdPref", Context.MODE_PRIVATE)
                .getInt("deviceId", 0).toString())

        binding.appVersionTv.text = getString(R.string.app_version_display, BuildConfig.VERSION_NAME)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                estViewModel.establishmentFlow.collect {
                    toggleLoadingMode(false)
                    when (it) {
                        is UIResult.FAILURE -> {
                            ProcessPhoenix.triggerRebirth(requireContext())
                        }
                        UIResult.LOADING -> {}
                        is UIResult.SUCCESS<*> -> {
                            val est = it.value as EstablishmentUIState
                            binding.numberPad.setMaxLength((est.tableCount ?: 99).toString().length)
                            binding.establishmentDisplayTv.text =
                                getString(R.string.establishment_display, est.name)
                            tableCount = est.tableCount ?: 65
                        }
                    }
                }
            }
        }


        binding.reloadButton.setOnClickListener()
        {
            toggleLoadingMode(true)
            Handler(Looper.getMainLooper()).postDelayed({
                estViewModel.reloadAds()
            }, 2000)
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }

        binding.tableNumTv.text =
            activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)
                ?.getString(getString(R.string.table_num_shared_preferences), "")

        setDebugButtonText()

        binding.debugButton.setOnClickListener()
        {
            val debugMode: Boolean =
                activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)
                    ?.getBoolean(getString(R.string.debug_mode_shared_preferences), false) ?: false

            sharedPref?.edit {
                this.putBoolean(getString(R.string.debug_mode_shared_preferences), !debugMode)
                this.apply()
            }

            setDebugButtonText()
        }

        binding.numberPad.setEventListener(object : NumberPadView.EventListener {
            override fun onTextChangedListener(text: String) {
                binding.tableNumTv.text = text
            }

            override fun onEnterCallback(text: String) {
                super.onEnterCallback(text)
                val toastMessage: String =
                    if (text.isEmpty()) {
                        "Please enter a number before pressing enter"
                    }
                    else if (text.toInt() < 1 || text.toInt() > tableCount) {
                        "Table number must be within 1 & $tableCount"
                    } else {
                        sharedPref?.edit {
                            this.putString(getString(R.string.table_num_shared_preferences), text)
                            this.apply()
                        }
                        "Table number has been changed to $text"
                    }
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_LONG).show()
            }
        })

        return binding.root
    }

    override fun setInitTheme(theme: Theme) {
        super.setInitTheme(theme)
        binding.root.background = context?.let { it1 ->
            ContextCompat.getDrawable(
                it1,
                theme.hubSettingsAppGradientTheme.drawableGradient
            )
        }
    }

}