package com.example.hub_os_device.ui.mainActivity.fragments

import Theme
import ThemeManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hub_os_device.R
import com.example.hub_os_device.databinding.FragmentMenuBinding
import com.example.hub_os_device.ui.mainActivity.MenuRecyclerViewAdapter
import com.example.hub_os_device.ui.mainActivity.viewModel.MenuFragmentViewModel
import com.example.hub_os_device.ui.mainActivity.viewModel.MenuItemsListUIState
import com.example.hub_os_device.ui.models.UIResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuFragment() : Fragment(), Parcelable, ThemeManager.ThemeChangedListener {

    lateinit var binding: FragmentMenuBinding
    private val viewModel: MenuFragmentViewModel by viewModels()

    constructor(parcel: Parcel) : this() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.addListener(this)
    }

    override fun onDestroy() {
        ThemeManager.removeListener(this)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentMenuBinding.inflate(layoutInflater)
        setInitTheme()

        binding.actionButton.setOnClickListener()
        {
            toggleViewVisibility(true)
            viewModel.getMenu()
        }

        binding.menuItemRecyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.menuInfoFlow.collect()
                {
                    when (it) {
                        UIResult.LOADING -> toggleViewVisibility(true)
                        is UIResult.FAILURE -> {
                            binding.errorTv.text = it.message
                            toggleViewVisibility(false)
                        }
                        is UIResult.SUCCESS<*> -> {
                            val value = (it.value as MenuItemsListUIState)
                            binding.establishmentNameTv.text =
                                getString(R.string.restaurant_menu, value.establishmentName)
                            binding.menuItemRecyclerView.adapter =
                                MenuRecyclerViewAdapter(value.list)
                            toggleViewVisibility(false, isSuccess = true)
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun toggleViewVisibility(isLoading: Boolean, isSuccess: Boolean = false) {
        binding.prgBar.visibility = if (isLoading && !isSuccess) View.VISIBLE else View.INVISIBLE
        binding.errorLl.visibility = if (!isLoading && !isSuccess) View.VISIBLE else View.INVISIBLE
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MenuFragment> {
        override fun createFromParcel(parcel: Parcel): MenuFragment {
            return MenuFragment(parcel)
        }

        override fun newArray(size: Int): Array<MenuFragment?> {
            return arrayOfNulls(size)
        }
    }

    override fun setInitTheme(theme: Theme) {
        super.setInitTheme(theme)
        binding.menuItemRecyclerView.background = context?.let { it1 ->
            ContextCompat.getDrawable(
                it1,
                theme.menuAppsColorTheme.backgroundColor
            )
        }
        binding.errorTv.setTextColor(ContextCompat.getColor(requireContext(),
            theme.menuAppsColorTheme.textColor))
    }
}