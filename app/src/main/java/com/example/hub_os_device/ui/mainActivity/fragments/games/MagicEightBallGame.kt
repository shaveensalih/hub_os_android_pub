package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.content.res.TypedArray
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.hub_os_device.databinding.FragmentMagicEightBallGameBinding

class MagicEightBallGame : Fragment() {

    lateinit var binding: FragmentMagicEightBallGameBinding
    lateinit var images: TypedArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMagicEightBallGameBinding.inflate(layoutInflater)
        images = resources.obtainTypedArray(com.example.hub_os_device.R.array.eight_ball_options)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val animShake: Animation =
            AnimationUtils.loadAnimation(context, com.example.hub_os_device.R.anim.shake_animation)
        val animFadeIn: Animation = AlphaAnimation(0f, 1f)
        animFadeIn.duration = 1000
        val animFadeOut: Animation = AlphaAnimation(1f, 0f)
        animFadeOut.duration = 300
        animFadeIn.fillAfter = true
        animFadeOut.fillAfter = true


        animShake.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                changeShakeStatus(animFadeOut, false)
            }

            override fun onAnimationEnd(p0: Animation?) {
                val choice = (Math.random() * images.length()).toInt()
                binding.eightBallTriangleIv.setImageResource(images.getResourceId(choice, 0))
                changeShakeStatus(animFadeIn, true)
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }

        })

        binding.eightBallShakeButton.setOnClickListener {
            binding.eightBallIv.startAnimation(animShake)
        }

        return binding.root
    }

    override fun onDestroy() {
        images.recycle()
        super.onDestroy()
    }

    private fun changeShakeStatus(anim: Animation, isButtonClickable: Boolean) {
        binding.eightBallTriangleIv.startAnimation(anim)
        binding.eightBallShakeButton.isClickable = isButtonClickable
    }
}