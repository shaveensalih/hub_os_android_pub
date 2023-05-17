package com.example.hub_os_device.ui.mainActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hub_os_device.databinding.MenuPopupItemBinding
import com.example.hub_os_device.ui.mainActivity.viewModel.ServiceableItemUIState

class PopUpMenuRecyclerViewAdapter(val context: Context, val selfServiceItems: List<ServiceableItemUIState>, val width: Int) :
    RecyclerView.Adapter<PopUpMenuRecyclerViewAdapter.MenuItemViewHolder>() {

    private var selectedItem: Int = -1
    var callback: RecyclerviewCallbacks<ServiceableItemUIState>? = null

    interface RecyclerviewCallbacks<T> {
        fun onItemClick(view: View, item: T)
    }

    inner class MenuItemViewHolder(private val binding: MenuPopupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        lateinit var item: ServiceableItemUIState
        fun bindItem(item: ServiceableItemUIState) {
            this.item = item
            binding.root.layoutParams.width = width
            binding.root.isSelected = true
            binding.root.text = item.name
        }
        init {
            setClickListener(binding.root)
        }

        private fun setClickListener(view: View,) {
            view.setOnClickListener {
                callback?.onItemClick(it, item)
            }
        }
    }

    fun setOnClick(callbacks: (item: ServiceableItemUIState) -> Unit){
        callback = object : RecyclerviewCallbacks<ServiceableItemUIState>
        {
            override fun onItemClick(view: View, item: ServiceableItemUIState) {
                callbacks(item)
            }

        }
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, p1: Int) {
        val item = selfServiceItems[p1]
        holder.bindItem(item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MenuItemViewHolder {
        return MenuItemViewHolder(
            MenuPopupItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return selfServiceItems.size
    }

}