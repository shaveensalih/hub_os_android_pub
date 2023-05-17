package com.example.hub_os_device.ui.mainActivity.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentWouldYouRatherBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.WouldYouRatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WouldYouRatherFragment : Fragment() {

    lateinit var binding: FragmentWouldYouRatherBinding
    val viewModel: WouldYouRatherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWouldYouRatherBinding.inflate(layoutInflater)

        lifecycleScope.launch()
        {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {

                viewModel.questionsFlow.collectLatest()
                {
                    toggleLoading(false)
                    binding.questionTitleTv.text =
                        it?.question ?: getString(R.string.would_you_rather_welcome)
                }
            }
        }

        binding.root.setOnClickListener {
            viewModel.getQuest()
            toggleLoading(true)
            binding.instructionsTv.text = getString(R.string.tap_for_another_question)
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.prgBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        binding.questionTitleTv.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.instructionsTv.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

}