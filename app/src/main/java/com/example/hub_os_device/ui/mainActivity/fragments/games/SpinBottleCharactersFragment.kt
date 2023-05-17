package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentSpinBottleCharactersBinding
import com.example.hub_os_device.ui.components.CharacterCard

private const val NUM_PLAYERS = "num_players"

val spinBottleColors: List<Int> = listOf(
    R.color.spin_bottle_blue,
    R.color.spin_bottle_orange,
    R.color.spin_bottle_purple,
    R.color.spin_bottle_pink,
    R.color.spin_bottle_green,
    R.color.spin_bottle_yellow,
    R.color.spin_bottle_red,
    R.color.spin_bottle_dark_blue,
)

val imageIds: Map<String, Int> = mapOf(
    "av1" to R.drawable.av1,
    "av2" to R.drawable.av2,
    "av3" to R.drawable.av3,
    "av4" to R.drawable.av4,
    "av5" to R.drawable.av5,
    "av6" to R.drawable.av6,
    "av7" to R.drawable.av7,
    "av8" to R.drawable.av8,
    "av9" to R.drawable.av9,
    "av10" to R.drawable.av10,
    "av11" to R.drawable.av11,
    "av12" to R.drawable.av12,
    "av13" to R.drawable.av13,
    "av14" to R.drawable.av14,
    "av15" to R.drawable.av15,
    "av16" to R.drawable.av16,
    "av17" to R.drawable.av17,
    "av18" to R.drawable.av18,
)

class SpinBottleCharactersFragment : Fragment() {
    private var currentColor: Int = spinBottleColors[0]
    private var numPlayers: Int? = null
    lateinit var binding: FragmentSpinBottleCharactersBinding
    var turn = 0
    var chosenCharacter: CharacterCard? = null
    var chars: ArrayList<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            numPlayers = it.getInt(NUM_PLAYERS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSpinBottleCharactersBinding.inflate(layoutInflater)
        binding.nextButton.isClickable = false

        if (numPlayers!! <= 4) binding.playersCardGrid.columnCount = 1

        for (i in 3..8)
            binding.playersCardGrid[i - 1].visibility = if (i > numPlayers!!) View.GONE else View.VISIBLE


        binding.nextButton.setOnClickListener {
            if (chosenCharacter != null) {
                imageIds[chosenCharacter?.tag]?.let { it1 -> chars.add(it1) }
                if (turn >= numPlayers!! - 1) {
                    val newScreen = SpinBottleFragment.newInstance(chars)
                    parentFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.app_content_container, newScreen)
                        .setReorderingAllowed(true)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                    return@setOnClickListener
                }
                (binding.playersCardGrid[turn] as ImageView).setImageDrawable(chosenCharacter!!.drawable)
                turn = ++turn
                currentColor = spinBottleColors[turn]
                binding.pickerPlayerIv.background.setTint(ContextCompat.getColor(requireContext(),
                    currentColor))
                binding.pickerPlayerIv.setImageDrawable(null)
                binding.pickerPlayerTv.text = getString(R.string.player_num, turn + 1)
                chosenCharacter?.isClickable = false
                chosenCharacter = null
                binding.nextButton.isClickable = false
            }
        }

        binding.characterGrid.forEach { card ->
            card.setOnClickListener {
                binding.nextButton.isClickable = true
                chosenCharacter?.resetCard()
                chosenCharacter = it as CharacterCard
                chosenCharacter!!.changeCardBg(currentColor)
                binding.pickerPlayerIv.setImageDrawable(it.drawable)
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(num_players: Int) =
            SpinBottleCharactersFragment().apply {
                arguments = Bundle().apply {
                    putInt(NUM_PLAYERS, num_players)
                }
            }
    }
}