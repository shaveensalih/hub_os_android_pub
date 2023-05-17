package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentFlipCoinBinding


class FlipCoinFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentFlipCoinBinding
    var isHeads = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentFlipCoinBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.root.setOnClickListener {
            val animation: CoinFlipAnimation
            val coinImage: ImageView = binding.flipCoinIv

            animation =
                CoinFlipAnimation(
                    coinImage,
                    if (isHeads) R.drawable.coin_heads else R.drawable.coin_tails,
                    if (isHeads) R.drawable.coin_tails else R.drawable.coin_heads,
                    0f,
                    180f,
                    0f,
                    0f,
                    0f,
                    0f
                )

            animation.repeatCount = (4..7).random()
            animation.duration = 200
            animation.interpolator = AccelerateDecelerateInterpolator()
            coinImage.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                    binding.coinResultTv.visibility = View.INVISIBLE
                    binding.flipCoinInstructions.visibility = View.INVISIBLE
                }

                override fun onAnimationEnd(p0: Animation?) {
                    binding.coinResultTv.visibility = View.VISIBLE
                    binding.flipCoinInstructions.visibility = View.VISIBLE
                    binding.coinResultTv.text = if (animation.isHeads) "Heads" else "Tails"
                    isHeads = animation.isHeads
                    binding.flipCoinInstructions.text = getString(R.string.flip_coin_again)
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }

            })
        }

        return binding.root
    }

    override fun onClick(p0: View?) {

        println("On CLick")

    }
}