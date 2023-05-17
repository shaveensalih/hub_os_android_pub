package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentTriviaCategoriesBinding
import com.example.hub_os_device.model.TriviaCategory
import com.example.hub_os_device.ui.mainActivity.viewModel.TriviaQuestionsState
import com.example.hub_os_device.ui.mainActivity.viewModel.TriviaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TriviaCategoriesFragment : Fragment() {

    private val progressMessages: List<String> =
        listOf(
            "Pro Tip: Pick the correct answer!",
            "Thinking really hard for questions...",
            "Are you ready to get your game on?",
            "Pro Tip: Don't pick the wrong answer...",
            "Searching the cosmos for questions and answers...",
            "Looking for quick quirky questions...",
            "Pro Tip: Keep playing for a top score!",
            "Warning: Questions may be extremely difficult... or not.\n I don't know I'm just a loading screen...",
            "Computing your chance of success... I predict a 9/10...",
            "Take a mindfulness minute before the trial...",
            "Computing the questions and answers to life, the universe, and everything!",
            "Trivia generator is generating...",
            "Did you know? If you get ten out of ten you win absolutely nothing!",
        )

    lateinit var binding: FragmentTriviaCategoriesBinding
    val viewModel: TriviaViewModel by viewModels({ requireParentFragment() })
    private var chosenCategory: String? = null
    var chosenCategoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println(requireParentFragment())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentTriviaCategoriesBinding.inflate(layoutInflater)

        parentFragmentManager.setFragmentResult(
            getString(R.string.changeTriviaTitlesKey),
            bundleOf(
                getString(R.string.title) to "Welcome to Trivia",
                getString(R.string.instructions) to "Choose a category to start the trivia trial."
            )
        )

        binding.categoryRg.onSelectionChanged { view ->
            binding.nextButton.isEnabled = true
            chosenCategory = view.tag as String
            chosenCategoryName = (view as RadioButton).text.toString()
        }

        binding.nextButton.setOnClickListener {
            binding.questionsPrgBar.progress = 0
            binding.loadingTv.typeWrite(this, progressMessages.random())
            binding.questionsPrgBar.visibility = View.VISIBLE
            binding.loadingTv.visibility = View.VISIBLE
            binding.categoryRg.visibility = View.INVISIBLE
            binding.nextButton.visibility = View.INVISIBLE
            TriviaCategory.valueOf(chosenCategory!!).let { it2 -> viewModel.getQuest(it2) }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.questionsFlow.onEach()
                {
                    when (it) {
                        is TriviaQuestionsState.SUCCESS -> {
                            parentFragmentManager
                                .beginTransaction()
                                .replace(
                                    R.id.trivia_container_view,
                                    TriviaQuestionFragment.newInstance(questionNumber = 0, chosenCategoryName)
                                )
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .setReorderingAllowed(true)
                                .commit()
                        }
                        is TriviaQuestionsState.FAILURE -> {
                            showCategories()
                            Toast.makeText(
                                context, "There has been an error. ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            showCategories()
                        }
                    }
                }.launchIn(this)

                viewModel.questionProgressFlow.onEach {
                    val progressAnimator =
                        ObjectAnimator.ofInt(binding.questionsPrgBar, "progress", (it * 10) - 10, it * 10)
                    progressAnimator.duration = 200
                    progressAnimator.interpolator = LinearInterpolator()
                    progressAnimator.start()
                }.launchIn(this)
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun showCategories() {
        binding.questionsPrgBar.visibility = View.INVISIBLE
        binding.loadingTv.visibility = View.INVISIBLE
        binding.categoryRg.visibility = View.VISIBLE
        binding.nextButton.visibility = View.VISIBLE
    }

    private fun TextView.typeWrite(lifecycleOwner: LifecycleOwner, text: String) {
        this@typeWrite.text = ""
        lifecycleOwner.lifecycleScope.launch {
            repeat(text.length) {
                delay(25)
                this@typeWrite.text = text.take(it + 1)
            }
        }
    }

}