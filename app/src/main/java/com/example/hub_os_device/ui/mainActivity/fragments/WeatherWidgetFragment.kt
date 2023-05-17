package com.example.hub_os_device.ui.mainActivity.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentWeatherWidgetBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WeatherWidgetFragment : Fragment() {

    var showRetry: Boolean = false

    companion object {
        fun newInstance() = WeatherWidgetFragment()
    }

    private val viewModel: WeatherWidgetViewModel by activityViewModels()
    private lateinit var binding: FragmentWeatherWidgetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("Making frag")
        binding = FragmentWeatherWidgetBinding.inflate(layoutInflater)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {

                binding.retryButton.setOnClickListener {
                    hideWeather(loading = true)
                    Handler(Looper.getMainLooper()).postDelayed(
                        { viewModel.getWeather(requireActivity().applicationContext) },
                        3000
                    )
                }

                viewModel.weeksWeatherFlow.onEach {
                    when (it) {
                        WeatherState.LOADING -> {
                            hideWeather(loading = true)
                        }
                        is WeatherState.SUCCESS<List<WeeklyWeatherUIState>> -> {
                            showWeather()
                            if (arguments?.getBoolean(
                                    getString(R.string.showWeekWeather),
                                    false
                                ) == true
                            ) {
                                binding.weekWeatherLl.visibility = View.VISIBLE
                                for (weather in it.successValue ?: listOf()) {
                                    println()
                                    val view =
                                        inflater.inflate(R.layout.weekly_weather_text_item, null)
                                    view.findViewById<TextView>(R.id.day_tv).text =
                                        weather.day.lowercase().replaceFirstChar { char ->
                                            char.uppercase()
                                        }
                                    view.findViewById<TextView>(R.id.temp_tv).text =
                                        weather.temp.roundToInt().toString()

                                    val lp = LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    lp.weight = 1F

                                    view.rootView.layoutParams = lp


                                    binding.weekWeatherLl.addView(view)

                                }
                            }
                        }
                        is WeatherState.FAILURE -> {
                            hideWeather(loading = false)
                        }
                    }
                }.launchIn(this)

                viewModel.currentWeatherFlow.onEach {
                    if (it != null) {
                        showWeather()
                        binding.weatherTv.text =
                            getString(R.string.degreeSymbol, it.temp.roundToInt().toString())
                        binding.weatherDescTv.text = it.desc
                        val myImage: Drawable? = context?.let { it1 ->
                            ResourcesCompat.getDrawable(
                                it1.resources,
                                getWeatherCodeImage(it.weatherCode), null
                            )
                        }
                        binding.weatherImage.setImageDrawable(myImage)
                    }
                }.launchIn(this)
            }
        }
        return binding.root
    }

    private fun showWeather() {
        binding.retryButton.visibility = View.INVISIBLE
        binding.weatherProgressBar.visibility = View.INVISIBLE
        binding.weatherInfoLayout.visibility = View.VISIBLE
    }

    private fun hideWeather(loading: Boolean) {
        binding.retryButton.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        binding.weatherProgressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        binding.weatherInfoLayout.visibility = View.INVISIBLE
        binding.weekWeatherLl.visibility = View.INVISIBLE
    }

    private fun getWeatherCodeImage(conditionCode: Int): Int {
        when (conditionCode) {
            in 200..299 -> {
                return R.drawable.thunderstorm
            }
            in 300..399 -> {
                return R.drawable.rain
            }
            in 500..599 -> {
                return R.drawable.rain
            }
            in 600..699 -> {
                return R.drawable.snow
            }
            in 700..799 -> {
                return R.drawable.rain
            }
            in 801..804 -> {
                return R.drawable.clouds
            }
            800 -> {
                return R.drawable.sunny
            }
            else -> {
                return R.drawable.sunny
            }
        }
    }

}