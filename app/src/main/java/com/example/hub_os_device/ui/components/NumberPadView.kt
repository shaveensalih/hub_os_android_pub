package com.example.hub_os_device.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IdRes
import com.example.hub_os_device.R


open class NumberPadView : FrameLayout, View.OnClickListener {
    private var mPasswordField: String = ""
    private var actionButtonType: ActionButtonType = ActionButtonType.Clear
    private var maxLength: Int? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.number_pad_widget, this)
        setCustomOnClickListeners(this)
    }

    interface EventListener {
        fun onTextChangedListener(text: String)
        fun onEnterCallback(text: String) {}
    }

    private var eventListener: EventListener? = null

    fun setEventListener(mEventListener: EventListener?) {
        this.eventListener = mEventListener
    }

    fun clearText() {
        mPasswordField = ""
        eventListener?.onTextChangedListener(mPasswordField)
    }

    fun setActionButtonType(actionButtonType: ActionButtonType) {
        this.actionButtonType = actionButtonType
        (getView<View>(R.id.t9_key_action_tv) as TextView).text = actionButtonType.toString()
    }

    fun setMaxLength(maxLength: Int) {
        this.maxLength = maxLength
    }

    private fun setCustomOnClickListeners(listener: OnClickListener) {
        getView<View>(R.id.t9_key_0).setOnClickListener(listener)
        getView<View>(R.id.t9_key_1).setOnClickListener(listener)
        getView<View>(R.id.t9_key_2).setOnClickListener(listener)
        getView<View>(R.id.t9_key_3).setOnClickListener(listener)
        getView<View>(R.id.t9_key_4).setOnClickListener(listener)
        getView<View>(R.id.t9_key_5).setOnClickListener(listener)
        getView<View>(R.id.t9_key_6).setOnClickListener(listener)
        getView<View>(R.id.t9_key_7).setOnClickListener(listener)
        getView<View>(R.id.t9_key_8).setOnClickListener(listener)
        getView<View>(R.id.t9_key_9).setOnClickListener(listener)
        getView<View>(R.id.t9_key_action).setOnClickListener(listener)
        getView<View>(R.id.t9_key_backspace).setOnClickListener(listener)
    }


    override fun onClick(v: View) {
        // handle number button click
        if (v.tag != null) {
            if (maxLength != null && mPasswordField.length >= maxLength!!) return
            mPasswordField += v.tag
            eventListener?.onTextChangedListener(mPasswordField)
            return
        }
        when (v.id) {
            R.id.t9_key_action -> {
                when (actionButtonType) {
                    ActionButtonType.Clear -> {
                        mPasswordField = ""
                    }
                    ActionButtonType.Enter -> {
                        eventListener?.onEnterCallback(mPasswordField)
                    }
                }
                // handle clear button
            }
            R.id.t9_key_backspace -> {
                // handle backspace button
                // delete one character
                val editable = mPasswordField
                val charCount = editable.length
                if (charCount > 0) {
                    mPasswordField = mPasswordField.dropLast(1)
                }
            }
        }
        eventListener?.onTextChangedListener(mPasswordField)
    }

    val inputText: String
        get() = mPasswordField

    private fun <T : View?> getView(@IdRes id: Int): T {
        return super.findViewById<View>(id) as T
    }
}

sealed interface ActionButtonType {
    object Clear : ActionButtonType {
        override fun toString(): String {
            return "Clear"
        }
    }

    object Enter : ActionButtonType {
        override fun toString(): String {
            return "Enter"
        }
    }
}