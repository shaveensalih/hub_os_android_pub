package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentHubAppLockScreenBinding
import com.example.hub_os_device.ui.components.NumberPadView
import com.example.hub_os_device.ui.mainActivity.viewModel.EstablishmentUIState
import com.example.hub_os_device.ui.mainActivity.viewModel.EstablishmentViewModel
import com.example.hub_os_device.ui.models.UIResult
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HubAppLockScreenFragment() : Fragment(), Parcelable, ThemeManager.ThemeChangedListener {

    lateinit var binding: FragmentHubAppLockScreenBinding
    private val estViewModel: EstablishmentViewModel by activityViewModels()


    constructor(parcel: Parcel) : this() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.transitionName =
            arguments?.getString(getString(R.string.transitionName))
    }

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
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHubAppLockScreenBinding.inflate(layoutInflater)
        setInitTheme()
        binding.numberPad.visibility = View.INVISIBLE
        binding.passcodeInputView.visibility = View.INVISIBLE

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                estViewModel.establishmentFlow.collectLatest {
                    when (it) {
                        is UIResult.FAILURE -> {
                            ProcessPhoenix.triggerRebirth(requireContext())
                        }
                        UIResult.LOADING -> {}
                        is UIResult.SUCCESS<*> -> {
                            val est = it.value as EstablishmentUIState
                            binding.establishmentDisplayTv.text =
                                getString(R.string.establishment_display, est.name)
                            binding.numberPad.visibility = View.VISIBLE
                            binding.passcodeInputView.visibility = View.VISIBLE
                            binding.numberPad.setEventListener(object : NumberPadView.EventListener {
                                override fun onTextChangedListener(text: String) {
                                    binding.passcodeInputView.setNumOfFilledSlots(text.length)
                                    if (text.length == 6) {
                                        if (estViewModel.checkPinCodesMatch(text)) {
                                            val newScreen = HubAppSettingsFragment()
                                            //Check if this matches the passcode
                                            parentFragmentManager
                                                .beginTransaction()
                                                .addToBackStack(null)
                                                .replace(R.id.app_content_container, newScreen)
                                                .setReorderingAllowed(true)
                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                                .commit()
                                        } else {
                                            binding.numberPad.clearText()
                                        }

                                    }
                                }
                            })
                        }
                    }
                }
            }
        }


        return binding.root
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HubAppLockScreenFragment> {
        override fun createFromParcel(parcel: Parcel): HubAppLockScreenFragment {
            return HubAppLockScreenFragment(parcel)
        }

        override fun newArray(size: Int): Array<HubAppLockScreenFragment?> {
            return arrayOfNulls(size)
        }
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