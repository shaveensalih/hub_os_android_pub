package com.example.hub_os_device.ui.mainActivity.fragments

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.SplitBillDialogBinding
import com.example.hub_os_device.ui.LooperInterface
import com.example.hub_os_device.ui.components.NumberPicker
import kotlin.math.roundToInt

class SplitBillDialogFragment : DialogFragment(), LooperInterface {
    lateinit var binding: SplitBillDialogBinding
    var total: Double = 0.0
    var selectedItemsTotal: Double = 0.0

    override var mHandler: Handler? = Handler(Looper.getMainLooper())
    override var mRunnable: Runnable? = null
    override var mTime: Long? = 30000

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        total = arguments?.getDouble("total")!!
        selectedItemsTotal = arguments?.getDouble("selected_items_total")!!
        mRunnable = Runnable {
            this.dismiss()
        }
        startHandler()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHandler()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SplitBillDialogBinding.inflate(layoutInflater)
        binding.root.setOnInterceptEvent {
            setFragmentResult("resetScheduler", bundleOf())
            stopHandler()
            startHandler()
        }
        binding.splitBillBetweenResultTv.text =
            getString(R.string.split_bill_result, "")
        binding.splitBillBetweenNp.setMinNumber(1)
        binding.splitBillBetweenNp.setMaxNumber(30)

        binding.splitBillBetweenNp.setEventListener(object : NumberPicker.EventListener {
            override fun onCountChangedListener(count: Int) {
                onRadioButtonsClicked(if (binding.selectedItemOption.isChecked) binding.selectedItemOption else binding.allItemsOption)
            }

        })

        binding.allItemsOption.setOnClickListener {
            onRadioButtonsClicked(it)
        }
        binding.selectedItemOption.setOnClickListener {
            onRadioButtonsClicked(it)
        }

        return binding.root
    }

    private fun onRadioButtonsClicked(view: View) {

        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked
            binding.splitBillBetweenNp.visibility = View.VISIBLE

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.selected_item_option ->
                    if (checked) {
                        binding.splitBillBetweenResultTv.text = getString(
                            R.string.split_bill_result,
                            getString(
                                R.string.split_bill_IQD_result,
                                (selectedItemsTotal / binding.splitBillBetweenNp.count).roundToInt()
                                    .toString()
                            )
                        )
                    }
                R.id.all_items_option ->
                    if (checked) {
                        binding.splitBillBetweenResultTv.text =
                            getString(
                                R.string.split_bill_result,
                                getString(
                                    R.string.split_bill_IQD_result,
                                    (total / binding.splitBillBetweenNp.count).roundToInt().toString()
                                )
                            )
                    }
            }
        }
    }
}