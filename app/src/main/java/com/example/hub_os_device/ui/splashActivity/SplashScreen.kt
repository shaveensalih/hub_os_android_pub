package com.example.hub_os_device.ui.splashActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.BuildConfig
import com.example.hub_os_device.PinCodeSplashDialog
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.ActivitySplashScreenBinding
import com.example.hub_os_device.databinding.HubSplashErrorBinding
import com.example.hub_os_device.model.Config
import com.example.hub_os_device.ui.mainActivity.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var errorView: HubSplashErrorBinding
    private val splashViewModel: SplashViewModel by viewModels()
    private var curConfig: Config? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}
        Intent().also { intent ->
            intent.action = "wangjx.action.broadcast.kidtablet"
            intent.putExtra("flag", true)
            sendBroadcast(intent)
        }
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        errorView = binding.hubSplashError

        errorView.settingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }

        toggleVisibleView(true)
        errorView.root.visibility = View.VISIBLE


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                splashViewModel.config.onEach()
                { config ->
                    when (config) {
                        ConfigState.LOADING -> {
                            toggleVisibleView(true)
                        }
                        is ConfigState.DEVICENOTREGISTERED -> {
                            changeErrorTexts(R.string.register, R.string.register_message)

                            errorView.actionButton.setOnClickListener()
                            {
                                lateinit var pinCodeSplashDialog: PinCodeSplashDialog
                                pinCodeSplashDialog =
                                    PinCodeSplashDialog.newInstance(null) { _, pin ->
                                        splashViewModel.registerDevice(pin)
                                        pinCodeSplashDialog.dismiss()
                                        toggleVisibleView(true)
                                    }
                                pinCodeSplashDialog.show(supportFragmentManager, "splash_pin_dialog")
                            }
                            toggleVisibleView(false)
                        }
                        is ConfigState.UPDATENEEDED -> {
                            curConfig = config.config

                            changeErrorTexts(R.string.update_hubos, R.string.update_app_message)

                            errorView.actionButton.setOnClickListener()
                            {
                                val pinCodeSplashDialog: PinCodeSplashDialog =
                                    PinCodeSplashDialog.newInstance(config.config.app.updatePinCode, "Please enter the update code") { _, _ ->

                                        val permission = ContextCompat.checkSelfPermission(
                                            applicationContext,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )

                                        if (permission == PackageManager.PERMISSION_GRANTED) {
                                            downloadNewApk(config.config)
                                        } else {
                                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        }
                                    }

                                pinCodeSplashDialog.show(supportFragmentManager, "splash_pin_dialog")
                            }
                            toggleVisibleView(false)

                        }
                        is ConfigState.SUCCESS -> {

                            // Check if saved is id null or its device id is not valid here.
                            val intent = Intent(this@SplashScreen, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        }
                        is ConfigState.FAILURE -> {
                            showFailureView(config.message)
                        }
                    }
                }.launchIn(this)

                splashViewModel.registerState.onEach {
                    when (it) {
                        is RegisterState.INCORRECTPIN ->
                            Snackbar.make(binding.root,
                                "Register pin code incorrect. Please try again.",
                                Snackbar.LENGTH_LONG).show()
                        is RegisterState.FAILURE -> showFailureView(it.message)
                        null -> {}
                    }
                }.launchIn(this)
            }
        }

    }

    private fun showFailureView(errorMessage: String) {
        changeErrorTexts(R.string.retry,
            null,
            getString(R.string.splash_screen_error_message, errorMessage))

        errorView.actionButton.setOnClickListener {
            toggleVisibleView(true)
            Handler(Looper.getMainLooper()).postDelayed({ splashViewModel.makeInitialCalls() },
                3000)
        }

        toggleVisibleView(false)
    }

    private fun toggleVisibleView(isLoading: Boolean, isUpdating: Boolean = false) {
        errorView.retryProgressBar.visibility = isVisible(isLoading)
        errorView.llErrorView.visibility = isVisible(!isLoading && !isUpdating)
        errorView.downloadProgressBar.visibility = isVisible(isUpdating)
    }

    private fun changeErrorTexts(button: Int, messageResource: Int?, messageString: String? = null) {
        errorView.errorTv.text = messageResource?.let { getString(it) } ?: messageString
        errorView.actionButton.text = getString(button)
    }

    private fun isVisible(visibility: Boolean) =
        if (visibility) View.VISIBLE else View.INVISIBLE

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            curConfig?.let { downloadNewApk(it) }
        } else {
            Toast.makeText(
                applicationContext,
                "In order to download new updates you must allow the permission to write to external storage. Tap update to try again or allow the permission in Settings.",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun downloadNewApk(config: Config) {
        val file = File(
            Environment.getExternalStorageDirectory(), "app.apk"
        )

        if (file.exists())
            file.delete()

        val uri =
            FileProvider.getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider", file)
        val url: String = config.app.appUrl

        splashViewModel.getNewApk(url, file)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                splashViewModel.apkState.collect()
                {
                    when (it) {
                        is APKState.DOWNLOADING<*> -> {
                            println(it.progress)
                            toggleVisibleView(isLoading = false, isUpdating = true)
                            errorView.downloadProgressBar.progress = it.progress as Int
                            errorView.errorTv.text =
                                String.format(
                                    getString(R.string.splash_screen_downloading_updating_message),
                                    config.app.latestVersion,
                                )
                        }
                        is APKState.FAILURE -> {
                            toggleVisibleView(false)
                            errorView.errorTv.text = String.format(
                                getString(R.string.splash_screen_update_error_message),
                                config.app.latestVersion,
                                it.message
                            )
                        }
                        is APKState.SUCCESS -> {
                            toggleVisibleView(false)
                            val install = Intent(Intent.ACTION_VIEW)
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            install.data = uri
                            startActivity(install)
                        }
                        null -> {}
                    }
                }
            }
        }
    }
}