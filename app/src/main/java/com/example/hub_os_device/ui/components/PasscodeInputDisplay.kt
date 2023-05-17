package com.example.hub_os_device.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.example.hub_os_device.R

class PasscodeInputDisplay : LinearLayout {

    lateinit var view: View

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes

        view = inflate(context, R.layout.passcode_input_display, this)
        fillInSlots(0)
    }

    fun setNumOfFilledSlots(num: Int) {
        fillInSlots(num)
        invalidate()
        requestLayout()
    }

    private fun fillInSlots(num: Int) {
        for ((index, value) in view.findViewById<LinearLayout>(R.id.passcode_input_ll).children.withIndex()) {
            if (index < num) {
                value.findViewById<View>(R.id.circle_center).visibility = View.VISIBLE
            } else {
                value.findViewById<View>(R.id.circle_center).visibility = View.INVISIBLE
            }

        }
    }

}