package com.example.hub_os_device.ui.mainActivity.fragments

import com.example.hub_os_device.ui.mainActivity.PopUpMenuRecyclerViewAdapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentSelfServiceBinding
import com.example.hub_os_device.databinding.MenuPopupBinding
import com.example.hub_os_device.ui.LooperInterface
import com.example.hub_os_device.ui.mainActivity.viewModel.SelfServiceViewModel
import com.example.hub_os_device.ui.mainActivity.viewModel.ServiceableItemUIState
import com.example.hub_os_device.ui.models.UIResult
import com.example.hub_os_device.util.parsePrice
import kotlinx.coroutines.launch


class SelfServiceFragment : Fragment(), LooperInterface {
    lateinit var binding: FragmentSelfServiceBinding
    private lateinit var popupBinding: MenuPopupBinding

    private val viewModel: SelfServiceViewModel by activityViewModels()

    override var mHandler: Handler? = Handler(Looper.getMainLooper())
    override var mRunnable: Runnable? = null
    override var mTime: Long? = 30000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelfServiceBinding.inflate(layoutInflater)
        popupBinding = MenuPopupBinding.inflate(layoutInflater)

        binding.root.setOnClickListener {
            if (viewModel.serviceableItemsFlow.value is UIResult.FAILURE) {
                viewModel.getMenu()
            }
            createPopup(binding.root)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.serviceableItemsFlow.collect()
                {
                    when (it) {
                        is UIResult.FAILURE -> {
                            popupBinding.unavailableTv.text = it.message
                            togglePopupListVisibility(false)
                        }
                        UIResult.LOADING -> {
                            togglePopupListVisibility(true)
                        }
                        is UIResult.SUCCESS<*> -> {
                            val items = it.value as List<ServiceableItemUIState>
                            if (items.isEmpty()) {
                                popupBinding.unavailableTv.text = getString(R.string.no_order_items_message)
                                togglePopupListVisibility(false)
                            } else {
                                togglePopupListVisibility(false, isSuccess = true)
                                val recyclerView = popupBinding.popupRv
                                val adapter =
                                    PopUpMenuRecyclerViewAdapter(requireContext(),
                                        items,
                                        requireView().width - (popupBinding.popupRv.marginStart + popupBinding.popupRv.marginEnd))
                                recyclerView.adapter = adapter
                                adapter.setOnClick { menuItem ->
                                    val dialog =
                                        ConfirmationDialog.newInstance(menuItem.name,
                                            "${parsePrice(menuItem.price.toInt())}IQD",
                                            menuItem.imageUrl,
                                            object : ConfirmationDialog.DialogListener {
                                                override fun onConfirm() {
                                                    super.onConfirm()
                                                    viewModel.orderItem(menuItem.id)
                                                }

                                                override fun describeContents(): Int {
                                                    return 0
                                                }

                                                override fun writeToParcel(p0: Parcel, p1: Int) {}

                                            })
                                    dialog.isCancelable = false
                                    dialog.show(parentFragmentManager, null)
                                }
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun createPopup(view: View) {
        if(popupBinding.root.parent != null) (popupBinding.root.parent as? ViewGroup)?.removeView(popupBinding.root)
        val popupWindow = PopupWindow(popupBinding.root)
        mRunnable = Runnable {
            popupWindow.dismiss()
        }
        startHandler()
        popupWindow.setOnDismissListener {
            stopHandler()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.width = view.width
        val locations = IntArray(2)
        view.getLocationInWindow(locations)
        popupBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popupWindow.showAsDropDown(view, 0, -(view.height + popupBinding.root.measuredHeight + 5))
    }

    private fun togglePopupListVisibility(isLoading: Boolean, isSuccess: Boolean = false) {
        popupBinding.noItemIconIv.visibility = if (!isLoading && !isSuccess) View.VISIBLE else View.INVISIBLE
        popupBinding.unavailableTv.visibility = if (!isLoading && !isSuccess) View.VISIBLE else View.INVISIBLE
        popupBinding.popupRv.visibility = if (isSuccess) View.VISIBLE else View.INVISIBLE
        popupBinding.prgBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }
}