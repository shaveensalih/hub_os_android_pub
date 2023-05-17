package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentDailyHoroscopeCategoriesBinding
import com.example.hub_os_device.model.StarSign
import com.example.hub_os_device.ui.mainActivity.viewModel.HoroscopeState
import com.example.hub_os_device.ui.mainActivity.viewModel.HoroscopeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DailyHoroscopeCategoriesFragment : Fragment() {
    lateinit var binding: FragmentDailyHoroscopeCategoriesBinding
    var chosenCategory: String? = null
    var chosenCategoryName: String? = null
    val viewModel: HoroscopeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentDailyHoroscopeCategoriesBinding.inflate(layoutInflater)

        binding.categoryRg.onSelectionChanged { view ->
            binding.nextButton.isEnabled = true
            chosenCategory = view.tag as String
            chosenCategoryName = (view as RadioButton).text.toString()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.horoscopeFlow.collectLatest()
                {
                    when (it) {
                        is HoroscopeState.SUCCESS -> {
                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.app_content_container,
                                    DailyHoroscopeResultFragment.newInstance(chosenCategoryName!!,
                                        getIcon(StarSign.valueOf(chosenCategory!!)),
                                        it.horoscope)
                                )
                                .setReorderingAllowed(true)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commit()
                        }
                        is HoroscopeState.FAILURE -> {
                            Toast.makeText(
                                requireContext(), "There has been an error. ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            toggleLoading(true)
                        }
                        else -> {
                            toggleLoading(false)
                        }
                    }
                }
            }
        }

        binding.nextButton.setOnClickListener {
            viewModel.getHoroscope(StarSign.valueOf(chosenCategory!!))
            toggleLoading(false)
        }

        return binding.root
    }

    private fun getIcon(sign: StarSign): Int {
        return when (sign) {
            StarSign.Aries -> R.drawable.ic_horoscope_aries
            StarSign.Taurus -> R.drawable.ic_horoscope_taurus
            StarSign.Gemini -> R.drawable.ic_horoscope_gemini
            StarSign.Cancer -> R.drawable.ic_horoscope_cancer
            StarSign.Leo -> R.drawable.ic_horoscope_leo
            StarSign.Virgo -> R.drawable.ic_horoscope_virgo
            StarSign.Libra -> R.drawable.ic_horoscope_libra
            StarSign.Scorpio -> R.drawable.ic_horoscope_scorpio
            StarSign.Sagittarius -> R.drawable.ic_horoscope_sagittarius
            StarSign.Capricorn -> R.drawable.ic_horoscope_capricorn
            StarSign.Aquarius -> R.drawable.ic_horoscope_aquarius
            StarSign.Pisces -> R.drawable.ic_horoscope_pisces
        }
    }

    private fun toggleLoading(categoriesVisible: Boolean) {
        binding.prgBar.visibility = if (categoriesVisible) View.INVISIBLE else View.VISIBLE
        binding.categoryRg.visibility = if (categoriesVisible) View.VISIBLE else View.INVISIBLE
        binding.nextButton.visibility = if (categoriesVisible) View.VISIBLE else View.INVISIBLE
    }

}