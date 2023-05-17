package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentTriviaBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.TriviaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TriviaFragment : Fragment() {

    val viewModel: TriviaViewModel by viewModels()
    lateinit var binding: FragmentTriviaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener(
            getString(R.string.changeTriviaTitlesKey),
            this
        ) { _, bundle ->
            binding.triviaQuestionTitle.text = bundle.getString(getString(R.string.title))
            binding.categoryInstructionsTv.text = bundle.getString(getString(R.string.instructions))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTriviaBinding.inflate(layoutInflater)

        return binding.root
    }
}