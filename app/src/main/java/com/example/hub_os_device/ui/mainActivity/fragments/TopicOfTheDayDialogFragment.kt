package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.TopicsDialogBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.TopicOfDayViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TopicOfTheDayDialogFragment : DialogFragment(), ThemeManager.ThemeChangedListener {
    lateinit var binding: TopicsDialogBinding
    private val topicOfDayViewModel: TopicOfDayViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = TopicsDialogBinding.inflate(layoutInflater)
        setInitTheme()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                topicOfDayViewModel.topicOfDayFlow.collectLatest {
                    binding.topicTv.text = it ?: getString(R.string.emptyString)
                }
            }
        }


        return binding.root
    }

    override fun setInitTheme(theme: Theme) {
        super.setInitTheme(theme)
        binding.dialogConstraintLayout.background =
            context?.let { ContextCompat.getDrawable(it, theme.dialogColorTheme.backgroundColor) }
        binding.topicTv.setTextColor(resources.getColor(theme.dialogColorTheme.textColor))
        binding.topicPageTitleTv.setTextColor(resources.getColor(theme.dialogColorTheme.textColor))
    }
}