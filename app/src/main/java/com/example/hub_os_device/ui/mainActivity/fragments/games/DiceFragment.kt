package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentDiceBinding

class DiceFragment : Fragment() {
    private lateinit var binding: FragmentDiceBinding

    private val dies = listOf(R.drawable.ic_dice_1,
        R.drawable.ic_dice_2,
        R.drawable.ic_dice_3,
        R.drawable.ic_dice_4,
        R.drawable.ic_dice_5,
        R.drawable.ic_dice_6)

    private val timer = object : CountDownTimer(800, 90) {
        override fun onTick(p0: Long) {
            changeDiceImages()
        }

        override fun onFinish() {
            changeDiceImages()
            binding.root.isClickable = true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDiceBinding.inflate(layoutInflater)
        val animShake: Animation =
            AnimationUtils.loadAnimation(context, R.anim.shake_animation)
        animShake.repeatCount = 4

        binding.root.setOnClickListener {
            binding.root.isClickable = false
            binding.diceIv1.startAnimation(animShake)
            binding.diceIv2.startAnimation(animShake)
            timer.start()
        }

        return binding.root
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    fun changeDiceImages() {
        binding.diceIv1.setImageDrawable(ContextCompat.getDrawable(requireContext(), dies.random()))
        binding.diceIv2.setImageDrawable(ContextCompat.getDrawable(requireContext(), dies.random()))
    }
}