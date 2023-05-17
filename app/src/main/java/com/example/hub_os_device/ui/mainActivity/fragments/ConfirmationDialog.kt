package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.hub_os_device.databinding.FragmentConfirmationDialogBinding
import com.example.hub_os_device.ui.LooperInterface
import com.example.hub_os_device.ui.mainActivity.viewModel.SelfServiceViewModel
import com.example.hub_os_device.ui.models.UIResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask

private const val CONFIRM_TEXT = "confirm_text"
private const val CONFIRM_ACTION = "confirm_action"
private const val CONFIRM_PRICE = "confirm_price"
private const val CONFIRM_IMAGE = "confirm_image"

class ConfirmationDialog : DialogFragment(), ThemeManager.ThemeChangedListener, LooperInterface {
    private var confirmText: String? = null
    private var confirmPrice: String? = null
    private var confirmImage: String? = null
    private var dialogListener: DialogListener? = null
    lateinit var binding: FragmentConfirmationDialogBinding
    val viewModel: SelfServiceViewModel by activityViewModels()

    override var mHandler: Handler? = Handler(Looper.getMainLooper())
    override var mRunnable: Runnable? = null
    override var mTime: Long? = 30000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRunnable = Runnable {
            this.dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        arguments?.let {
            confirmText = it.getString(CONFIRM_TEXT)
            confirmPrice = it.getString(CONFIRM_PRICE)
            dialogListener = it.getParcelable(CONFIRM_ACTION)
            confirmImage = it.getString(CONFIRM_IMAGE)
        }
        return dialog
    }

    override fun onDestroy() {
        stopHandler()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentConfirmationDialogBinding.inflate(layoutInflater)
        setInitTheme()
        startHandler()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.selfServiceResultFlow.collectLatest {
                    when (it) {
                        is UIResult.FAILURE -> {
                            toggleViews(false)
                            Toast.makeText(requireContext(),
                                "Oops... Could not order item due to: ${it.message}",
                                Toast.LENGTH_LONG).show()
                            startHandler()
                        }
                        UIResult.LOADING -> {
                            toggleViews(true)
                        }
                        is UIResult.SUCCESS<*> -> {
                            Timer().schedule(timerTask {
                                dialog?.cancel()
                            }, 2000)
                            toggleViews(false, isSuccess = true)
                        }
                    }
                }
            }
        }

        binding.actionDescTv.text = confirmText
        binding.actionPriceTv.text = confirmPrice
        if (confirmImage != null) {
            binding.itemIv.visibility = View.VISIBLE
            val image = Glide.with(this)
                .load(confirmImage)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.itemIv)
        }
        binding.cancelButton.setOnClickListener {
            dialog?.cancel()
        }

        binding.orderButton.setOnClickListener {
            stopHandler()
            toggleViews(true)
            dialogListener?.onConfirm()
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(text: String?, price: String?, image: String?, listener: DialogListener?) =
            ConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putString(CONFIRM_TEXT, text)
                    putString(CONFIRM_PRICE, price)
                    putString(CONFIRM_IMAGE, image)
                    putParcelable(CONFIRM_ACTION, listener)
                }
            }
    }

    interface DialogListener : Parcelable {
        fun onConfirm() {}
    }

    override fun setInitTheme(theme: Theme) {
        super.setInitTheme(theme)
        binding.confirmTitleTv.setTextColor(getColor(theme.dialogColorTheme.textColor))
        binding.actionDescTv.setTextColor(getColor(theme.dialogColorTheme.textColor))
        binding.parentCl.background =
            ContextCompat.getDrawable(requireContext(), theme.dialogColorTheme.backgroundColor)
    }

    fun getColor(color: Int): Int {
        return ContextCompat.getColor(requireContext(),
            color)
    }

    private fun toggleViews(isLoading: Boolean, isSuccess: Boolean = false) {
        binding.orderViewCl.visibility = if (!isLoading && !isSuccess) View.VISIBLE else View.INVISIBLE
        binding.orderSuccessLl.visibility = if (isSuccess) View.VISIBLE else View.INVISIBLE
        binding.prgBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }
}