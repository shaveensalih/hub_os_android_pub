package com.example.hub_os_device.ui.mainActivity

import Theme
import ThemeManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.example.hub_os_device.databinding.MenuCategoryTitleBinding
import com.example.hub_os_device.databinding.MenuItemBinding
import com.example.hub_os_device.databinding.MenuSectionTitleBinding
import com.example.hub_os_device.model.MenuItem
import com.example.hub_os_device.ui.mainActivity.viewModel.MenuItemUIState
import com.example.hub_os_device.ui.mainActivity.viewModel.MenuType
import com.example.hub_os_device.util.parsePrice

class MenuRecyclerViewAdapter constructor(
    private val menu: List<MenuItemUIState>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MenuItemViewHolder(private val itemBinding: MenuItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root), ThemeManager.ThemeChangedListener {
        fun bindItem(menuItem: MenuItem) {
            setInitTheme()
            ThemeManager.addListener(this)
            itemBinding.itemNameTv.text = menuItem.name
            itemBinding.itemPriceTv.text = parsePrice(menuItem.price.toInt())
            itemBinding.itemDescTv.text = menuItem.desc
        }

        override fun setInitTheme(theme: Theme) {
            super.setInitTheme(theme)
            itemBinding.root.children.forEach {
                (it as TextView).setTextColor(itemView.resources.getColor(theme.menuAppsColorTheme.textColor))
            }
        }
    }

    inner class MenuCategoryTitleViewHolder(private val itemBinding: MenuCategoryTitleBinding) :
        RecyclerView.ViewHolder(itemBinding.root), ThemeManager.ThemeChangedListener {
        fun bindItem(title: String) {
            setInitTheme()
            ThemeManager.addListener(this)
            itemBinding.categoryTitle.text = title
        }

        override fun setInitTheme(theme: Theme) {
            super.setInitTheme(theme)
            itemBinding.root.children.forEach {
                if (it is TextView)
                    it.setTextColor(itemView.resources.getColor(theme.menuAppsColorTheme.textColor))
                else
                    (it).background =
                        ContextCompat.getDrawable(itemView.context, theme.menuAppsColorTheme.textColor)
            }
        }
    }

    inner class MenuSectionTitleViewHolder(private val itemBinding: MenuSectionTitleBinding) :
        RecyclerView.ViewHolder(itemBinding.root), ThemeManager.ThemeChangedListener {
        fun bindItem(title: String) {
            setInitTheme()
            ThemeManager.addListener(this)
            itemBinding.sectionTitle.text = title
        }

        override fun setInitTheme(theme: Theme) {
            super.setInitTheme(theme)
            itemBinding.root.children.forEach {
                (it as TextView).setTextColor(itemView.resources.getColor(theme.menuAppsColorTheme.textColor))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (menu[position].type) {
            MenuType.SECTION -> 0
            MenuType.CATEGORY -> 1
            MenuType.ITEM -> 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> MenuSectionTitleViewHolder(
                MenuSectionTitleBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                ),
            )
            1 -> MenuCategoryTitleViewHolder(
                MenuCategoryTitleBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                ),
            )
            2 -> MenuItemViewHolder(
                MenuItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> {
                MenuCategoryTitleViewHolder(
                    MenuCategoryTitleBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    ),
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = menu[position]
        when (item.type) {
            MenuType.SECTION -> (holder as MenuSectionTitleViewHolder).bindItem(item.item as String)
            MenuType.CATEGORY -> (holder as MenuCategoryTitleViewHolder).bindItem(item.item as String)
            MenuType.ITEM -> (holder as MenuItemViewHolder).bindItem(item.item as MenuItem)
        }
    }

    override fun getItemCount(): Int {
        return menu.size
    }


}