package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import com.example.hub_os_device.R


class CoinFlipAnimation(
    private val imageView: ImageView,
    private var curDrawable: Int,
    private var nextDrawable: Int,
    private val fromXDegrees: Float,
    private val toXDegrees: Float,
    private val fromYDegrees: Float,
    private val toYDegrees: Float,
    private val fromZDegrees: Float,
    private val toZDegrees: Float
) :
    Animation() {
    private var camera: Camera? = null
    private var width = 0
    private var height = 0
    private var numOfRepetition = 0
    private var repeatCount = 0f
    var isHeads: Boolean = true

    override fun setRepeatCount(repeatCount: Int) {
        super.setRepeatCount(repeatCount)
        this.repeatCount = (repeatCount + 1).toFloat()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        this.width = width / 2
        this.height = height / 2
        camera = Camera()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        var xDegrees = fromXDegrees + (toXDegrees - fromXDegrees) * interpolatedTime
        val yDegrees = fromYDegrees + (toYDegrees - fromYDegrees) * interpolatedTime
        val zDegrees = fromZDegrees + (toZDegrees - fromZDegrees) * interpolatedTime
        val matrix = t.matrix


        // ----------------- ZOOM ----------------- //
        if ((numOfRepetition + interpolatedTime) / (repeatCount / 2) <= 1) {
            imageView.scaleX = 1 + ((numOfRepetition + interpolatedTime) / (repeatCount / 2))
            imageView.scaleY = 1 + ((numOfRepetition + interpolatedTime) / (repeatCount / 2))
        } else if (numOfRepetition < repeatCount) {
            imageView.scaleX = 3 - ((numOfRepetition + interpolatedTime) / (repeatCount / 2))
            imageView.scaleY = 3 - ((numOfRepetition + interpolatedTime) / (repeatCount / 2))
        }


        // ----------------- ROTATE ----------------- //
//        System.err.println(interpolatedTime)
        if (interpolatedTime >= 0.5f) {
            if (interpolatedTime == 1f) {
                val temp = curDrawable
                curDrawable = nextDrawable
                nextDrawable = temp
                numOfRepetition++
                if (numOfRepetition == repeatCount.toInt())
                    isHeads = curDrawable == R.drawable.coin_heads
            } else {
                imageView.setImageResource(nextDrawable)
            }
            xDegrees -= 180f
        } else if (interpolatedTime == 0f) {
            imageView.setImageResource(curDrawable)
        }
        camera!!.save()
        camera!!.rotateX(-xDegrees)
        camera!!.rotateY(yDegrees)
        camera!!.rotateZ(zDegrees)
        camera!!.getMatrix(matrix)
        camera!!.restore()
        matrix.preTranslate(-width.toFloat(), -height.toFloat())
        matrix.postTranslate(width.toFloat(), height.toFloat())
    }
}