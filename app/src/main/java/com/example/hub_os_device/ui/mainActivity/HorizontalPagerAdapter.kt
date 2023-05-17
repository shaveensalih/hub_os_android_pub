package com.example.hub_os_device.ui.mainActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hub_os_device.ui.mainActivity.fragments.LargeAdPage
import com.example.hub_os_device.ui.mainActivity.fragments.SmallAdPage

open class HorizontalPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle,) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> {
                LargeAdPage()
            }
            1 -> {
                SmallAdPage()
            }
            else -> {
                LargeAdPage()
            }
        }
    }
}