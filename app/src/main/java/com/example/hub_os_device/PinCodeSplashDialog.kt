package com.example.hub_os_device

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.hub_os_device.databinding.FragmentPincodeSplashDialogBinding
import com.example.hub_os_device.ui.components.NumberPadView
import java.io.Serializable

private const val PIN_PARAM1 = "pin_param1"
private const val CALLBACK_PARAM2 = "callback_param2"
private const val PIN_INSTRUCTION_PARAM = "pin_instruction_param"

class PinCodeSplashDialog : DialogFragment(), NumberPadView.EventListener, Serializable {
    private var pin: String? = null
    private lateinit var binding: FragmentPincodeSplashDialogBinding
    private lateinit var callback: Listener
    private lateinit var pinMessage: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pin = it.getString(PIN_PARAM1)
            callback = it.getSerializable(CALLBACK_PARAM2) as Listener
            pinMessage = it.getString(PIN_INSTRUCTION_PARAM)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPincodeSplashDialogBinding.inflate(layoutInflater)
        binding.pinPasscodeInstructions.text = pinMessage
        binding.pinNumberPad.setMaxLength(6)

        binding.pinNumberPad.setEventListener(this)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(pin: String?, instructionText: String = "Please enter your establishment code", callback: (dialog: View, pin: String) -> Unit) =
            PinCodeSplashDialog().apply {
                arguments = Bundle().apply {
                    putString(PIN_PARAM1, pin)
                    putString(PIN_INSTRUCTION_PARAM, instructionText)
                    putSerializable(CALLBACK_PARAM2, Listener(callback))
                }
            }
    }

    override fun onTextChangedListener(text: String) {
        binding.pinPasscodeInput.setNumOfFilledSlots(text.length)
        if (text.length == 6) {
            if (text == pin || pin == null) {
                this.dismiss()
                callback.onClick(binding.root, text)
            } else {
                binding.pinNumberPad.clearText()
            }
        }
    }
}

class Listener(val clickAction: (dialog: View, pin: String) -> Unit) : Serializable {
    fun onClick(dialog: View, pin: String) = clickAction(dialog, pin)
}