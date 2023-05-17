package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentSpinBottlePlayerNumberChoiceBinding

class SpinBottlePlayerNumberChoice : Fragment() {

    lateinit var binding: FragmentSpinBottlePlayerNumberChoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSpinBottlePlayerNumberChoiceBinding.inflate(layoutInflater)

        binding.playerNumGrid.forEach { card ->
            card.setOnClickListener {
                val newScreen = SpinBottleCharactersFragment.newInstance(it.tag.toString().toInt())
                parentFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.app_content_container, newScreen)
                    .setReorderingAllowed(true)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
            }
        }

        return binding.root
    }

}