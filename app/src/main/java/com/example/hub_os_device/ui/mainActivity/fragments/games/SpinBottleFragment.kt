package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.example.hub_os_device.databinding.FragmentSpinBottleBinding
import com.example.hub_os_device.ui.components.SpinBottlePie


private const val PLAYER_IMAGES = "player_images"

class SpinBottleFragment : Fragment() {
    private var playerImages: ArrayList<Int>? = null
    lateinit var binding: FragmentSpinBottleBinding
    var currentAngle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playerImages = it.getIntegerArrayList(PLAYER_IMAGES)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSpinBottleBinding.inflate(layoutInflater)

        val spinBottlePie = SpinBottlePie(requireContext(), playerImages!!)
        binding.root.setOnClickListener()
        {
            binding.root.isClickable = false
            val rotation = (720..2160).random()
            currentAngle = (currentAngle + rotation) % 360

            binding.bottle.animate().rotationBy(rotation.toFloat()).setDuration(1000)
                .setInterpolator(DecelerateInterpolator()).setListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(p0: Animator) {
                        spinBottlePie.resetPie()
                    }

                    override fun onAnimationEnd(p0: Animator) {
                        spinBottlePie.drawPie(currentAngle)
                        binding.root.isClickable = true
                    }

                    override fun onAnimationCancel(p0: Animator) {
                    }

                    override fun onAnimationRepeat(p0: Animator) {
                    }


                })
        }

        binding.root.addView(spinBottlePie, 0)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(playerImages: ArrayList<Int>) =
            SpinBottleFragment().apply {
                arguments = Bundle().apply {
                    putIntegerArrayList(PLAYER_IMAGES, playerImages)
                }
            }
    }
}