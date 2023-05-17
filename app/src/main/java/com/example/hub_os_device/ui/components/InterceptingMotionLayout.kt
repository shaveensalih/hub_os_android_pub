package com.example.hub_os_device.ui.components

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.motion.widget.MotionLayout

class InterceptingMotionLayout : MotionLayout {

    var interceptCallback: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
    }

    override fun onDraw(canvas: Canvas) {

    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        super.onInterceptTouchEvent(event)
        interceptCallback?.invoke()
        return false
    }

    fun setOnInterceptEvent(callback: () -> Unit)
    {
        interceptCallback = callback
    }
}