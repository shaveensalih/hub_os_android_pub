package com.example.hub_os_device.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.hub_os_device.R
import com.google.android.material.card.MaterialCardView


class CharacterCard(context: Context, private val attrs: AttributeSet) :
    CardView(context, attrs), View.OnClickListener {

    var drawable: Drawable? = null
    var drawableId: Int? = null

    init {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        inflate(context, R.layout.spin_bottle_character_card, this)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.custom_attributes,
            0, 0
        ).apply {
            try {
                drawable = getDrawable(R.styleable.custom_attributes_drawable)
                getView<ImageView>(R.id.character_card_iv).setImageDrawable(drawable)
            } finally {
                recycle()
            }
        }
    }

    interface CharacterCardEventListener {
        fun onClickListener()
    }

    private var eventListener: CharacterCardEventListener? = null

    fun setEventListener(mEventListener: CharacterCardEventListener?) {
        this.eventListener = mEventListener
    }

    fun changeCardBg(color: Int) {
        getView<MaterialCardView>(R.id.char_card).setCardBackgroundColor(ContextCompat.getColor(context,
            color))
    }

    fun setOnClickListener(callback: () -> Unit) {
        this.eventListener = object : CharacterCardEventListener {
            override fun onClickListener() {
                callback()
            }

        }
    }

    fun resetCard() {
        getView<MaterialCardView>(R.id.char_card).setCardBackgroundColor(ContextCompat.getColor(context,
            R.color.white_faded))
    }

    override fun onClick(v: View) {
        eventListener?.onClickListener()
    }

    private fun <T : View?> getView(@IdRes id: Int): T {
        return super.findViewById<View>(id) as T
    }
}