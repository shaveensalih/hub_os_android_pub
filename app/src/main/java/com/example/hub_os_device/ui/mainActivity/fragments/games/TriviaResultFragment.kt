package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentTriviaResultBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.TriviaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TriviaResultFragment : Fragment() {

    lateinit var binding: FragmentTriviaResultBinding
    private val viewModel: TriviaViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTriviaResultBinding.inflate(layoutInflater)

        parentFragmentManager.setFragmentResult(
            getString(R.string.changeTriviaTitlesKey),
            bundleOf(
                getString(R.string.title) to "Trivia Quiz Complete",
                getString(R.string.instructions) to "Hooray! You have completed the trial!"
            )
        )

        binding.resultScoreTv.text = getString(R.string.number_out_of_ten, viewModel.score.toString())

        binding.resultDescTv.text = when (viewModel.score) {
            in 0..4 -> "Ouchie... Better luck next time!"
            in 5..6 -> "You passed!\n Try again for a higher score!"
            in 7..8 -> "You're a smart cookie!\n Play again for a perfect score!"
            9 -> "Stunning. Extraordinary. Unbeatable.\n Next could be perfect score..."
            10 -> "Woah a perfect 10/10 score!\n All hail the genius!"
            else -> ""
        }

        binding.playAgainButton.setOnClickListener {
            viewModel.resetTrivia()
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.trivia_container_view,
                    TriviaCategoriesFragment()
                )
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }

        return binding.root
    }
}