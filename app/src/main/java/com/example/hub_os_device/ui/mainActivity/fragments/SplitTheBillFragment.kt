package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentSplitTheBillBinding
import com.example.hub_os_device.ui.mainActivity.SplitTheBillRecyclerViewAdapter
import com.example.hub_os_device.ui.mainActivity.viewModel.BillItemUIState
import com.example.hub_os_device.ui.mainActivity.viewModel.SplitTheBillFragmentViewModel
import com.example.hub_os_device.ui.models.UIResult
import com.example.hub_os_device.util.parsePrice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class SplitTheBillFragment() : Fragment(), Parcelable, ThemeManager.ThemeChangedListener {

    lateinit var binding: FragmentSplitTheBillBinding
    private val viewModel: SplitTheBillFragmentViewModel by viewModels<SplitTheBillFragmentViewModel>()
    lateinit var currentBillList: List<BillItemUIState>


    constructor(parcel: Parcel) : this() {
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
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSplitTheBillBinding.inflate(layoutInflater)
        setInitTheme()


        binding.splitEvenlyButton.setOnClickListener {
            val splitBillDialogFragment = SplitBillDialogFragment()
            val args = Bundle()
            args.putDouble("total", currentBillList.sumOf { it.costSummary.total * it.quantity })
            args.putDouble("selected_items_total", viewModel.totalFlow.value.total)
            splitBillDialogFragment.arguments = args
            activity?.supportFragmentManager?.let { it1 ->
                splitBillDialogFragment.show(it1,
                    "dialog")
            }

        }

        binding.actionButton.setOnClickListener {
            toggleViewVisibility(true)
            viewModel.getBillInfo()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.billInfoUIStateFlow.onEach() {
                    when (it) {
                        is UIResult.SUCCESS<*> -> {
                            val billList = it.value as List<BillItemUIState>?
                            if (billList != null) {
                                currentBillList = billList
                                setupRecyclerView(billList)
                                toggleViewVisibility(isLoading = false, true)
                            }
                        }
                        is UIResult.FAILURE -> {
                            binding.errorTv.text = it.message
                            toggleViewVisibility(isLoading = false)
                        }
                        UIResult.LOADING -> {
                            toggleViewVisibility(true)
                        }
                    }
                }.launchIn(this)

                viewModel.totalFlow.onEach {
                    binding.selectedTv.text =
                        getString(R.string.selected, parsePrice(it.selected.roundToInt()))
                    binding.serviceTv.text = getString(R.string.service, parsePrice(it.service.roundToInt()))
                    if (it.vat == null && it.discount == null) changeDiscountVatVisibility(false)
                    else {
                        binding.appliedLaterTv.visibility = View.INVISIBLE
                        assignTextOf(binding.vatTv, it.vat, R.string.vat)
                        assignTextOf(binding.discountTv, it.discount, R.string.discount)
                    }
                    binding.totalTv.text = getString(R.string.total,  parsePrice(it.total.roundToInt()))
                }.launchIn(this)
            }

        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun changeDiscountVatVisibility(isVisible: Boolean) {
        binding.appliedLaterTv.visibility = if (isVisible) View.INVISIBLE else View.VISIBLE
        binding.vatTv.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        binding.discountTv.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    private fun setupRecyclerView(billList: List<BillItemUIState>?) {
        binding.splitTheBillRecyclerView.layoutManager =
            LinearLayoutManager(context)
        binding.splitTheBillRecyclerView.adapter =
            billList?.let {
                SplitTheBillRecyclerViewAdapter(it,
                    viewModel)
            }
        binding.splitTheBillRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun toggleViewVisibility(isLoading: Boolean, isSuccess: Boolean = false) {
        binding.prgBar.visibility = toggleVisibility(isLoading)
        binding.errorLl.visibility = toggleVisibility(!isLoading && !isSuccess)
        binding.splitTheBillRecyclerView.visibility = toggleVisibility(isSuccess)
        binding.totalBox.visibility = toggleVisibility(isSuccess)
    }

    private fun toggleVisibility(isVisible: Boolean): Int =
        if (isVisible) View.VISIBLE else View.INVISIBLE

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SplitTheBillFragment> {
        override fun createFromParcel(parcel: Parcel): SplitTheBillFragment {
            return SplitTheBillFragment(parcel)
        }

        override fun newArray(size: Int): Array<SplitTheBillFragment?> {
            return arrayOfNulls(size)
        }
    }

    private fun assignTextOf(tv: TextView, value: Double?, strResId: Int) {
        if (value != null) {
            tv.text = getString(strResId, parsePrice(value.roundToInt()))
            tv.visibility = if (value != 0.0) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun setInitTheme(theme: Theme) {
        super.setInitTheme(theme)
        binding.root.background = context?.let { it1 ->
            ContextCompat.getDrawable(
                it1,
                theme.menuAppsColorTheme.backgroundColor
            )
        }
        binding.errorTv.setTextColor(ContextCompat.getColor(requireContext(),
            theme.menuAppsColorTheme.textColor))
    }
}