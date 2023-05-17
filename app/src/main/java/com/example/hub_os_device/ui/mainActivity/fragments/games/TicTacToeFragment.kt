package com.example.hub_os_device.ui.mainActivity.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentTicTacToeBinding
import com.google.android.material.card.MaterialCardView

class TicTacToeFragment : Fragment() {

    lateinit var binding: FragmentTicTacToeBinding
    var turn: TicTacToeTurn = TicTacToeTurn.X
    var checkedBoxes = mutableMapOf<Int, TicTacToeTurn>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTicTacToeBinding.inflate(layoutInflater)

        //Configure reset button
        binding.playAgainButton.setOnClickListener {
            togglePlayButtonVisibility(false)
            applyToBoxes {
                ((it as MaterialCardView).children.first() as ImageView).setImageDrawable(null)
                it.isClickable = true
                checkedBoxes = mutableMapOf()
            }
        }
        applyToBoxes { card ->
            card.setOnClickListener {

                //Setting the X or O image.
                val xoDrawable =
                    if (turn == TicTacToeTurn.X) R.drawable.ic_x_mark else R.drawable.ic_o_mark
                val image = ((it as MaterialCardView).children.first() as ImageView)
                image.setImageDrawable(getDrawable(xoDrawable))
                image.colorFilter = null

                //Saving the position of the chosen box to save to the map
                val pos = (card.tag as String).toInt()
                checkedBoxes[pos] = turn

                //Checking for possible win or draw
                if (checkedBoxes.size >= 5) {
                    val winningLinePositions = checkWin(pos)

                    //Win Found
                    if (winningLinePositions != null) {
                        val winAnim: Animation =
                            loadAnimation(if (turn == TicTacToeTurn.O) R.anim.bounce_animation else R.anim.shake_animation)
                        winningLinePositions.forEach { winPos ->
                            //Animating and changing color of each winning symbol
                            showWin(winPos, winAnim)
                        }
                        incrementWinCounter()
                        addWinAnimListener(winAnim)
                        removeBoxesClickable()
                        return@setOnClickListener
                    } else if (checkedBoxes.size >= 9) {
                        binding.resultTv.visibility = View.VISIBLE
                        val slideAnim = loadAnimation(R.anim.slide_up)
                        binding.resultTv.startAnimation(slideAnim)
                        togglePlayButtonVisibility(true)
                        changeTurn()
                        removeBoxesClickable()
                        return@setOnClickListener
                    }
                }
                changeTurn()
                card.isClickable = false
            }

        }
        return binding.root
    }

    private fun addWinAnimListener(winAnim: Animation) {
        winAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                togglePlayButtonVisibility(true)
            }

            override fun onAnimationRepeat(p0: Animation?) {}
        })
    }

    private fun incrementWinCounter() {
        when (turn) {
            TicTacToeTurn.X -> (binding.xScoreTv.text.toString().toInt() + 1).toString()
                .also { binding.xScoreTv.text = it }
            TicTacToeTurn.O -> (binding.oScoreTv.text.toString()
                .toInt() + 1).toString().also { binding.oScoreTv.text = it }
        }
    }

    private fun showWin(it: Pair<Int, Int>, winAnim: Animation) {
        val row: TableRow =
            binding.ticTacToeTable.children.elementAt(it.first) as TableRow
        val image: ImageView =
            (row.children.elementAt(it.second) as MaterialCardView).children.first() as ImageView
        image.startAnimation(winAnim)
        ContextCompat.getColor(requireContext(), R.color.correct_green)
            .let { it1 -> image.setColorFilter(it1) }
    }

    private fun loadAnimation(animation: Int) = AnimationUtils.loadAnimation(context, animation)

    private fun getDrawable(drawable: Int) = ResourcesCompat.getDrawable(resources, drawable, null)

    private fun applyToBoxes(callback: (card: View) -> Unit) {
        binding.ticTacToeTable.children.forEach { row ->
            (row as TableRow).forEach {
                callback(it)
            }
        }
    }

    private fun togglePlayButtonVisibility(isVisible: Boolean) {
        binding.playerXTv.visibility = if (isVisible) View.INVISIBLE else View.VISIBLE
        binding.playerOTv.visibility = if (isVisible) View.INVISIBLE else View.VISIBLE
        binding.playAgainButton.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        if (!isVisible) binding.resultTv.visibility = View.INVISIBLE
    }

    private fun removeBoxesClickable() = applyToBoxes { it.isClickable = false }

    private fun checkWin(position: Int): List<Pair<Int, Int>>? {
        val row: Int = kotlin.math.floor((position / 3.0)).toInt()
        val col: Int = position % 3
        if (hasHorizontalWin(row)) return listOf(row to 0, row to 1, row to 2)
        if (hasVerticalWin(col)) return listOf(0 to col, 1 to col, 2 to col)
        if (isEven(position) && position != 4) {
            val axisDif: Int = kotlin.math.abs(position - 4)
            if (checkDiagonalWin(axisDif)) return listOf(4 - axisDif to 0, 1 to 1, (4 + axisDif) % 3 to 2)
        } else {
            if (checkDiagonalWin(4)) return listOf(0 to 0, 1 to 1, 2 to 2)
            if (checkDiagonalWin(2)) return listOf(0 to 2, 1 to 1, 2 to 0)
        }
        return null
    }

    private fun isEven(num: Int): Boolean = num % 2 == 0
    private fun checkSymbolFor(row: Int, col: Int): Boolean = checkedBoxes[getPos(row, col)] == turn
    private fun getPos(row: Int, col: Int): Int = (row * 3) + col

    private fun hasHorizontalWin(row: Int): Boolean =
        (checkSymbolFor(row, 0) && checkSymbolFor(row, 1) && checkSymbolFor(row, 2))

    private fun hasVerticalWin(col: Int): Boolean =
        (checkSymbolFor(0, col) && checkSymbolFor(1, col) && checkSymbolFor(2, col))

    private fun checkDiagonalWin(axisDif: Int): Boolean {
        return (checkSymbolFor(4 - axisDif, 0) && checkSymbolFor(1, 1) && checkSymbolFor(
            (4 + axisDif) % 3, 2
        ))
    }

    private fun changeTurn() {
        when (turn) {
            TicTacToeTurn.O -> {
                turn = TicTacToeTurn.X
                changeTurnIndicators(binding.playerXTv, binding.playerOTv)
            }
            TicTacToeTurn.X -> {
                turn = TicTacToeTurn.O
                changeTurnIndicators(binding.playerOTv, binding.playerXTv)
            }
        }
    }

    private fun changeTurnIndicators(curTurnTv: TextView, prevTurnTv: TextView) {
        curTurnTv.background = getDrawable(R.drawable.tic_tac_toe_bordered_option)
        curTurnTv.setTextColor(Color.WHITE)
        prevTurnTv.background = getDrawable(R.drawable.tic_tac_toe_non_bordered_option)
        prevTurnTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_faded))
    }
}

enum class TicTacToeTurn() {
    X,
    O
}