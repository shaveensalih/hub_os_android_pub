package com.example.hub_os_device.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class PopUpCardView : CardView {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {

    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {

        val height = MeasureSpec.makeMeasureSpec(300, MeasureSpec.AT_MOST)

        super.onMeasure(widthSpec, height)
    }


}