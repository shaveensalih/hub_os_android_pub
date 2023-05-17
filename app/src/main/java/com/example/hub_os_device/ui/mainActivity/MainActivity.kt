package com.example.hub_os_device.ui.mainActivity

import Theme
import ThemeManager
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.ActivityMainBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.AdsViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderScriptBlur
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val adsViewModel: AdsViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private fun adjustDayNightMode(preferenceManager: SharedPreferences) {
        val nightMode: Boolean =
            preferenceManager.getBoolean(getString(R.string.dayNightModePreference), false)
        ThemeManager.theme = if (nightMode) Theme.DARK else Theme.LIGHT
    }

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            ?.registerOnSharedPreferenceChangeListener(null)
        adsViewModel.stopTimer()
        super.onDestroy()
    }

    override fun onPause() {
        adsViewModel.stopTimer()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adsViewModel.startTimer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(applicationContext)
        firebaseAnalytics = Firebase.analytics
        adjustDayNightMode(PreferenceManager.getDefaultSharedPreferences(applicationContext))
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            ?.registerOnSharedPreferenceChangeListener(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.setFragmentResultListener("changePage", this) { requestKey, bundle ->
            val cardId: Int = bundle.getInt("page")
            changeViewPagerPage(cardId)
        }

        val decorView: View = window.decorView
        val windowBackground = decorView.background

        binding.blurView.setupWith(binding.root,
            RenderScriptBlur(this))
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(5f)

        //Connecting the viewpager to its adapter & keeping pages alive
        viewPager = binding.viewPager
        viewPager.adapter = HorizontalPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.offscreenPageLimit = 1
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 1)
                    firebaseAnalytics.logEvent("swipe", null)
            }
        })

        lifecycleScope.launch() {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adsViewModel.currentAdState.collect { currentAd ->
                    if (currentAd == null) {
                        binding.root.setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.hub_color
                            )
                        )
                    } else if (currentAd.videoUrl != null) {
                        loadImage(currentAd.videoUrl)
                    } else {
                        loadImage(currentAd.primaryImgUrl)
                    }
                }
            }
        }

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun loadImage(imageLink: String?) {
        Glide.with(this@MainActivity)
            .load(imageLink)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .transform(ColorFilterTransformation(0x66000000))
            .into(binding.bgImageView)
    }

    private fun changeViewPagerPage(page: Int) {
        binding.viewPager.currentItem = page
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p0 != null) adjustDayNightMode(p0)
    }
}