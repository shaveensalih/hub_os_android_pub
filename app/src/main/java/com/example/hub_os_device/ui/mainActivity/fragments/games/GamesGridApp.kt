package com.example.hub_os_device.ui.mainActivity.fragments.games

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.hub_os_device.R
import com.example.hub_os_device.ui.mainActivity.fragments.TicTacToeFragment
import com.example.hub_os_device.databinding.FragmentGamesGridAppBinding
import com.example.hub_os_device.ui.mainActivity.fragments.WouldYouRatherFragment
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

class GamesGridApp() : Fragment(), Parcelable {

    lateinit var binding: FragmentGamesGridAppBinding

    constructor(parcel: Parcel) : this() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = FragmentGamesGridAppBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val decorView: View = requireActivity().window.decorView
        val windowBackground = decorView.background

        binding.gameIconsGrid.forEach {

            if (it is CardView && it.children.first() is BlurView)
                (it.children.first() as BlurView).setupWith(
                    binding.root,
                    RenderScriptBlur(requireContext()))
                    .setFrameClearDrawable(windowBackground) // Optional
                    .setBlurRadius(20f)

            val newScreen = when (it.id) {
                R.id.eight_ball_icon -> MagicEightBallGame()
                R.id.flip_coin_icon -> FlipCoinFragment()
                R.id.trivia_icon -> TriviaFragment()
                R.id.tic_tac_toe_icon -> TicTacToeFragment()
                R.id.this_that_icon -> WouldYouRatherFragment()
                R.id.dice_roll_icon -> DiceFragment()
                R.id.spin_bottle_icon -> SpinBottlePlayerNumberChoice()
                R.id.horoscope_icon -> DailyHoroscopeCategoriesFragment()
                else -> MagicEightBallGame()
            }

            it.setOnClickListener {
                logGameEvent(it?.tag as String)
                //Check if this matches the passcode
                parentFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.app_content_container, newScreen)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .commit()
            }
        }



        return binding.root
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    private fun logGameEvent(gameTag: String) {
        Firebase.analytics.logEvent("game_tapped", bundleOf("game_tag" to gameTag))
    }

    companion object CREATOR : Parcelable.Creator<GamesGridApp> {
        override fun createFromParcel(parcel: Parcel): GamesGridApp {
            return GamesGridApp(parcel)
        }

        override fun newArray(size: Int): Array<GamesGridApp?> {
            return arrayOfNulls(size)
        }
    }
}