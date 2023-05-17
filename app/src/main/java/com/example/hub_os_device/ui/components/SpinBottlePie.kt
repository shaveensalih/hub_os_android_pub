package com.example.hub_os_device.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.hub_os_device.ui.mainActivity.fragments.games.spinBottleColors
import java.lang.Math.cos
import java.lang.Math.sin


class SpinBottlePie : View {

    private lateinit var images: ArrayList<Int>
    private var divisions: Int? = null
    private var bottleAngle: Int? = null
    private var angle: Double? = null
    private var angleInDegrees: Float? = null
    private var midPointx: Double? = null
    private var midPointy: Double? = null
    private lateinit var box: RectF
    private val deg270: Double = 4.71239
    private lateinit var piePaint: Paint

    private var multiplier: Double? = null
    private var alignmentConstant: Double? = null

    constructor(context: Context, images: ArrayList<Int>) : super(context) {
        init(null, 0)
        this.images = images

        divisions = images.size
        angle = 2 * (3.14 / divisions!!)
        angleInDegrees = (360.0 / images.size).toFloat()

        multiplier =
            if (divisions!! % 2 == 0) {
                //TODO - Figure out this strange multiplier equation for multiples of 4, 8, etc...
                if (divisions!! % 8 == 0)
                    3.75
                else if (divisions!! % 4 == 0)
                    2.5
                else
                    2.0

            } else 3.0

        alignmentConstant = if (divisions!! % 2 != 0)
            kotlin.math.ceil(divisions!!.toDouble() / 2)
        else {
            if (divisions!! % 4 != 0)
                (divisions!! / 2) - 1.0
            else 0.0
        }

        piePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        midPointx = width.toDouble() / 2
        midPointy = height.toDouble() / 2

        box = RectF(30f,
            (midPointy!! - (midPointx!! - 30)).toFloat(),
            canvas.width.toFloat() - 30,
            (midPointy!! + (midPointx!! - 30)).toFloat())

        images.forEachIndexed { i, image ->
            // x & y position calculation for the character images
            // Arc equation -> (𝑥,𝑦)= startingPos + (𝑟cos𝜑,𝑟sin𝜑).
            val x: Double = (midPointx!!) +
                    (height / 5) *
                    cos((deg270 * multiplier!!) + ((angle!! * (i + alignmentConstant!!))))
            val y: Double = (midPointy!!) +
                    (height / 5) *
                    sin((deg270 * multiplier!!) + ((angle!! * (i + alignmentConstant!!))))
            val startAngle = (i * angleInDegrees!!) - 90

            piePaint.style = Paint.Style.STROKE
            piePaint.color = Color.BLACK
            canvas.drawArc(box, startAngle, angleInDegrees!!, true, piePaint)

            val isChosen: Boolean? =
                if (bottleAngle == null) null else (bottleAngle!! >= startAngle && bottleAngle!! < startAngle + angleInDegrees!!)
            piePaint.style = Paint.Style.FILL
            piePaint.color =
                if (isChosen != null && !isChosen) Color.parseColor("#59000000") else ContextCompat.getColor(
                    context,
                    spinBottleColors[i % 8])
            canvas.drawCircle(x.toFloat(), y.toFloat(), 52.5f, piePaint)
            canvas.drawArc(box,
                startAngle,
                angleInDegrees!!,
                true,
                piePaint)
            val d = ContextCompat.getDrawable(context, image)
            d?.setBounds((x - 50).toInt(), (y - 50).toInt(), (x + 50).toInt(), (y + 50).toInt())
            d?.alpha = if (isChosen != null && !isChosen) 89 else 255
            d?.draw(canvas)
        }
    }

    fun drawPie(angle: Int) {
        bottleAngle = angle - 90
        invalidate()
    }

    fun resetPie() {
        bottleAngle = null
        invalidate()
    }
}