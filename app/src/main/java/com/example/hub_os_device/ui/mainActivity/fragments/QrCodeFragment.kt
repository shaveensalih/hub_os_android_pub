package com.example.hub_os_device.ui.mainActivity.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.QrCodeFragmentBinding
import com.example.hub_os_device.ui.QRCodeInterface
import com.example.hub_os_device.ui.mainActivity.viewModel.QRCodeFragmentViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class QrCodeFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener, QRCodeInterface {

    private lateinit var binding: QrCodeFragmentBinding
    private var showWifiCode by Delegates.notNull<Boolean>()

    private val qrCodeFragmentViewModel: QRCodeFragmentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)
            ?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE)
            ?.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = QrCodeFragmentBinding.inflate(layoutInflater)

        showWifiCode = arguments?.getBoolean(getString(R.string.showWifiCode)) == true

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                qrCodeFragmentViewModel.qrCodeInfoFlow.collectLatest {
                    if (showWifiCode) {
                        if (it?.wifiSSID != null) {
                            binding.wifiNameTv.text = getString(R.string.wifi_name_display, it.wifiSSID)
                            binding.wifiNameTv.visibility = View.VISIBLE
                        }
                        if (it?.wifiCode != null) {
                            binding.wifiCodeTv.text =
                                getString(
                                    R.string.wifi_pw_display,
                                    qrCodeFragmentViewModel.qrCodeInfoFlow.value!!.wifiCode
                                )
                        }
                    }

                    if (it?.dineOSIntegrated == false) binding.menuTV.text = getString(R.string.scan_for_menu)


                    assignTable(
                        activity?.getSharedPreferences("tableNumPreferences", Context.MODE_PRIVATE),
                        it?.qrCode
                    )
                }

            }
        }

        alterShownText(showWifiCode)

        return binding.root
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        assignTable(
            p0,
            qrCodeFragmentViewModel.qrCodeInfoFlow.value?.qrCode
        )
    }

    private fun assignTable(
        p0: SharedPreferences?,
        qrCode: String?,
    ) {

        val tableNum: String = p0
            ?.getString(getString(R.string.table_num_shared_preferences), "") ?: ""
        if (!showWifiCode) {
            binding.tableNumTv.text = if (tableNum.isNotEmpty()) getString(
                R.string.table_num_display, tableNum
            ) else "NO Table"
        }

        binding.qrCodeIV.post {
            val qr = getQrCodeBitmap(
                if (qrCode != null) "$qrCode?table=$tableNum"
                else "https://backspace.krd/", binding.qrCodeIV.width, binding.qrCodeIV.height
            )
            binding.qrCodeIV.setImageBitmap(qr)
        }

    }

    private fun alterShownText(showWifiCode: Boolean) {
        binding.wifiCodeTv.visibility = if (showWifiCode) View.VISIBLE else View.INVISIBLE
        binding.tableNumTv.visibility = if (!showWifiCode) View.VISIBLE else View.INVISIBLE
    }
}