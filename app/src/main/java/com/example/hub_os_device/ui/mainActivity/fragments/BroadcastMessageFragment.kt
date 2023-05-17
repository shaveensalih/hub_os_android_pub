package com.example.hub_os_device.ui.mainActivity.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentBroadcastMessageBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.BroadcastMessageFragmentViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BroadcastMessageFragment : Fragment() {

    lateinit var binding: FragmentBroadcastMessageBinding
    val viewModel: BroadcastMessageFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentBroadcastMessageBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.broadcastMessageFlow.collectLatest {
                    binding.marqueeTv.text = it ?: getString(R.string.hub_os_message)
                    binding.marqueeTv.isSelected = true
                }
            }
        }

        return binding.root
    }
}