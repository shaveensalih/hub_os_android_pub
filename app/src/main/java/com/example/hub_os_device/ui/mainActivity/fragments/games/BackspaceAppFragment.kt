package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.databinding.FragmentBackspaceAppBinding
import com.example.hub_os_device.ui.QRCodeInterface
import com.example.hub_os_device.ui.mainActivity.viewModel.BackspaceAppViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.g0dkar.qrcode.QRCode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BackspaceAppFragment() : Fragment(), Parcelable, QRCodeInterface {

    lateinit var binding: FragmentBackspaceAppBinding
    val viewModel: BackspaceAppViewModel by viewModels()

    constructor(parcel: Parcel) : this() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBackspaceAppBinding.inflate(layoutInflater)

        val instaIv = binding.instaQrIv
        val websiteIv = binding.websiteQrIv
        val contactIv = binding.contactQrIv

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.linksFlow.collectLatest {

                    val instaQr = makeQR(it.instagram ?: "http://www.instagram.com/backspace.krd")
                    val websiteQr = makeQR(it.website ?: "https://backspace.krd")
                    val contactQr =
                        makeQR(if (it.contact != null) "tel:${it.contact}" else "mailto:shad@backspace.com")

                    instaIv.setImageBitmap(instaQr)
                    websiteIv.setImageBitmap(websiteQr)
                    contactIv.setImageBitmap(contactQr)
                }
            }
        }


        return binding.root
    }

    private fun makeQR(link: String): Bitmap {
        return QRCode(link).render(
            brightColor = Color.BLACK,
            darkColor = Color.TRANSPARENT)
            .nativeImage() as Bitmap
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
    }

    companion object CREATOR : Parcelable.Creator<BackspaceAppFragment> {
        override fun createFromParcel(parcel: Parcel): BackspaceAppFragment {
            return BackspaceAppFragment(parcel)
        }

        override fun newArray(size: Int): Array<BackspaceAppFragment?> {
            return arrayOfNulls(size)
        }
    }
}