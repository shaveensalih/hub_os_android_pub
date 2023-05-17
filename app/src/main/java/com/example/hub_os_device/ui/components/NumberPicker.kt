package com.example.hub_os_device.ui.components

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.TouchDelegate
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.cardview.widget.CardView
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.NumberPickerBinding


class NumberPicker(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs),
    View.OnClickListener {
    private var maxNumber: Int = 100
    private var minNumber: Int = -100
    private var _count: Int = 0

    val binding: NumberPickerBinding

    init {
        val view = inflate(context, R.layout.number_picker, this)
        binding = NumberPickerBinding.bind(view)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.custom_attributes,
            0, 0
        ).apply {
            try {
                getView<CardView>(R.id.item_counter_button).setCardBackgroundColor(
                    getColor(
                        R.styleable.custom_attributes_exampleColor,
                        resources.getColor(R.color.hub_accent_color_main)
                    )
                )
            } finally {
                recycle()
            }
        }

        increaseTouchArea(binding.addButton)
        increaseTouchArea(binding.minusButton)
        setCustomOnClickListeners(this)
    }

    interface EventListener {
        fun onCountChangedListener(count: Int)
    }

    private var eventListener: EventListener? = null

    fun setEventListener(mEventListener: EventListener?) {
        this.eventListener = mEventListener
    }

    fun setMaxNumber(maxNumber: Int) {
        this.maxNumber = maxNumber
        if (_count > maxNumber) {
            _count = maxNumber
            updateCountText()
        }
    }

    fun setMinNumber(minNumber: Int) {
        this.minNumber = minNumber
        if (_count < minNumber) {
            _count = minNumber
            updateCountText()
        }
    }

    fun setMinMaxNumber(min: Int, max: Int) {
        setMinNumber(min)
        setMaxNumber(max)
    }

    fun incrementCount(by: Int) {
        _incrementCount(by)
    }

    private fun setCustomOnClickListeners(listener: OnClickListener) {
        getView<View>(R.id.add_button).setOnClickListener(listener)
        getView<View>(R.id.minus_button).setOnClickListener(listener)
    }


    override fun onClick(v: View) {
        val addedNum = if (v.id == R.id.add_button) 1 else -1

        _incrementCount(addedNum)
    }

    private fun _incrementCount(addedNum: Int) {
        if (isBetweenLimits(addedNum))
            _count += addedNum

        updateCountText()
    }

    private fun updateCountText() {
        getView<TextView>(R.id.item_quantity_tv).text = count.toString()

        eventListener?.onCountChangedListener(_count)
    }

    private fun isBetweenLimits(number: Int): Boolean {
        return (_count + number) <= maxNumber && (_count + number) >= minNumber
    }

    val count: Int
        get() = _count

    private fun <T : View?> getView(@IdRes id: Int): T {
        return super.findViewById<View>(id) as T
    }

    private fun increaseTouchArea(view: View) {
        val parent =
            view.parent as View

        parent.post {
            val rect = Rect()
            view.getHitRect(rect)
            rect.top -= 50
            rect.left -= 50
            rect.bottom += 50
            rect.right += 50
            parent.touchDelegate = TouchDelegate(rect, view)
        }
    }
}