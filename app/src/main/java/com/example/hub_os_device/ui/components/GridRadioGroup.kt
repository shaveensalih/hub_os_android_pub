package com.example.hub_os_device.ui.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.content.ContextCompat.getColor
import com.example.hub_os_device.R

class GridRadioGroup(context: Context?, attrs: AttributeSet?) : TableLayout(context, attrs),
    View.OnClickListener {
    private var activeRadioButton: RadioButton? = null
    private var radioButtonListener: RadioButtonListener? = null
    private var activeTextColor: Int? = null
    private var inactiveTextColor: Int? = null

    interface RadioButtonListener {
        fun onRadioButtonChanged(view: View)
    }

    init {
        context?.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.custom_attributes,
            0, 0
        )?.apply {
            try {
                activeTextColor =
                    getColor(
                        R.styleable.custom_attributes_activeTextColor,
                        resources.getColor(R.color.hub_accent_color_main)
                    )
                inactiveTextColor = getColor(R.styleable.custom_attributes_inactiveTextColor,
                    R.styleable.custom_attributes_inactiveTextColor)
            } finally {
                recycle()
            }
        }
    }

    override fun onClick(v: View) {
        val rb = v as RadioButton
        activeRadioButton?.isChecked = false
        activeRadioButton?.setTextColor(inactiveTextColor ?: getColor(context, R.color.hub_accent_color_main))
        rb.isChecked = true
        rb.setTextColor(activeTextColor ?: Color.WHITE)
        activeRadioButton = rb
        radioButtonListener?.onRadioButtonChanged(activeRadioButton!!)
    }

    override fun addView(
        child: View, index: Int,
        params: ViewGroup.LayoutParams,
    ) {
        super.addView(child, index, params)
        setChildrenOnClickListener(child as TableRow)
    }

    private fun setChildrenOnClickListener(tr: TableRow) {
        val c = tr.childCount
        for (i in 0 until c) {
            val v = tr.getChildAt(i)
            (v as? RadioButton)?.setOnClickListener(this)
        }
    }

    fun onSelectionChanged(listener: RadioButtonListener) {
        this.radioButtonListener = listener
    }

    fun onSelectionChanged(onChange: (view: View) -> Unit) {
        this.radioButtonListener = object : RadioButtonListener {
            override fun onRadioButtonChanged(view: View) {
                onChange(view)
            }
        }
    }

    val checkedRadioButtonId: Int
        get() = if (activeRadioButton != null) {
            activeRadioButton!!.id
        } else -1

    companion object {
        private const val TAG = "ToggleButtonGroupTableLayout"
    }
}