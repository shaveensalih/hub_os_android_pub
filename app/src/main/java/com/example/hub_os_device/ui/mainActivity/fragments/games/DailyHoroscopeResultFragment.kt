package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentDailyHoroscopeResultBinding
import com.example.hub_os_device.model.DailyHoroscope
import com.example.hub_os_device.ui.mainActivity.viewModel.HoroscopeState
import com.example.hub_os_device.ui.mainActivity.viewModel.HoroscopeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val HOROSCOPE_NAME = "horoscopeName"
private const val HOROSCOPE_IMAGE = "horoscopeImage"
private const val HOROSCOPE = "horoscope"

@AndroidEntryPoint
class DailyHoroscopeResultFragment : Fragment() {
    private var horoscopeName: String? = null
    private var horoscopeImage: Int? = null
    private var horoscope: DailyHoroscope? = null
    lateinit var binding: FragmentDailyHoroscopeResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            horoscopeName = it.getString(HOROSCOPE_NAME)
            horoscopeImage = it.getInt(HOROSCOPE_IMAGE)
            horoscope = it.getSerializable(HOROSCOPE) as DailyHoroscope
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDailyHoroscopeResultBinding.inflate(layoutInflater)

        binding.horoscopeTitleTv.text = horoscopeName
        binding.horoscopeIv.setImageDrawable(ContextCompat.getDrawable(requireContext(), horoscopeImage!!))
        binding.compatibilityTv.text = horoscope?.compatibility
        binding.colorTv.text = horoscope?.color
        binding.horoscopeDatesTv.text = horoscope?.dateRange
        binding.numberTv.text = horoscope?.luckyNumber
        binding.tipTv.text = horoscope?.description
        binding.timeTv.text = horoscope?.luckyTime
        binding.moodTv.text = horoscope?.mood
        binding.currentDateTv.text = horoscope?.currentDate

        binding.redoHoroscopeButton.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.app_content_container,
                    DailyHoroscopeCategoriesFragment()
                )
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(horoscopeName: String, image: Int, horoscope: DailyHoroscope) =
            DailyHoroscopeResultFragment().apply {
                arguments = Bundle().apply {
                    putString(HOROSCOPE_NAME, horoscopeName)
                    putInt(HOROSCOPE_IMAGE, image)
                    putSerializable(HOROSCOPE, horoscope)
                }
            }
    }
}