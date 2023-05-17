package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentTriviaQuestionBinding
import com.example.hub_os_device.model.TriviaQuestion
import com.example.hub_os_device.ui.mainActivity.viewModel.TriviaQuestionsState
import com.example.hub_os_device.ui.mainActivity.viewModel.TriviaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUESTION_NUMBER = "question_number"
private const val CATEGORY_NAME = "category_name"

@AndroidEntryPoint
class TriviaQuestionFragment : Fragment() {

    lateinit var binding: FragmentTriviaQuestionBinding
    private val viewModel: TriviaViewModel by viewModels({ requireParentFragment() })

    private var correctAnswerButton: RadioButton? = null
    private var chosenAnswerButton: RadioButton? = null
    private var answerChecked: Boolean = false

    private var questionNumber: Int? = null
    private var categoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println(requireParentFragment())
        arguments?.let {
            questionNumber = it.getInt(QUESTION_NUMBER)
            categoryName = it.getString(CATEGORY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentTriviaQuestionBinding.inflate(layoutInflater)

        if (categoryName != null) {
            parentFragmentManager.setFragmentResult(
                getString(R.string.changeTriviaTitlesKey),
                bundleOf(
                    getString(R.string.title) to "Trivia: $categoryName",
                    getString(R.string.instructions) to "Choose your answer, check it, then click next for another question."
                )
            )
        }

        val options: List<RadioButton> =
            listOf(binding.firstOption, binding.secondOption, binding.thirdOption, binding.fourthOption)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.questionsFlow.collectLatest {
                    when (it) {
                        is TriviaQuestionsState.SUCCESS -> {
                            val q = questionNumber?.let { it1 -> it.questions?.get(it1) }
                            correctAnswerButton = options[(0..3).random()]
                            binding.triviaQuestionTv.text = q?.question?.parseAsHtml()
                            binding.triviaDifficultyTv.text =
                                getString(R.string.trivia_difficulty, q?.difficulty)
                            binding.questionNumberTv.text =
                                getString(R.string.number_out_of_ten, (questionNumber?.plus(1)).toString())
                            fillInAnswers(q, options)
                        }
                        else -> {}
                    }
                }

            }
        }

        binding.answerRg.onSelectionChanged { view ->
            if (chosenAnswerButton == null) {
                binding.nextButton.isEnabled = true
                binding.nextButton.isClickable = true
            }
            chosenAnswerButton = view as RadioButton
        }

        binding.nextButton.setOnClickListener {
            if (chosenAnswerButton != null && !answerChecked) {
                answerChecked = true
                binding.nextButton.text =
                    getString(if (questionNumber!! < 9) R.string.next else R.string.finish)
                for (option in options) option.isClickable = false
                showButtonResult(correctAnswerButton!!, true)
                if (chosenAnswerButton != correctAnswerButton)
                    showButtonResult(chosenAnswerButton!!, false)
                else
                    viewModel.incrementScore()
            } else {
                val newQuestionNumber = questionNumber?.plus(1)
                parentFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.trivia_container_view,
                        if (questionNumber!! < 9) newInstance(
                            newQuestionNumber!!,
                            null
                        ) else TriviaResultFragment()
                    )
                    .setReorderingAllowed(true)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun showButtonResult(button: RadioButton, isCorrect: Boolean) {
        button.background =
            ResourcesCompat.getDrawable(resources, R.drawable.trivia_result_answer_button, null)
        button.isChecked = isCorrect
        button.setTextColor(Color.WHITE)
    }

    private fun fillInAnswers(
        q: TriviaQuestion?,
        options: List<RadioButton>,
    ) {
        var j = 0
        for (option in options) {
            option.text =
                if (option != correctAnswerButton) q!!.incorrectAnswers[j].parseAsHtml() else q?.correctAnswer?.parseAsHtml()
            if (option != correctAnswerButton) j++
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(questionNumber: Int, categoryName: String?) =
            TriviaQuestionFragment().apply {
                arguments = Bundle().apply {
                    putInt(QUESTION_NUMBER, questionNumber)
                    putString(CATEGORY_NAME, categoryName)
                }
            }
    }
}